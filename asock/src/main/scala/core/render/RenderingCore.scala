package core.render

import java.awt.Frame
import java.awt.event.InputEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.DataBufferByte
import java.io.File
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import scala.Array.canBuildFrom
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.promise
import com.hackoeur.jglm.Mat4
import com.hackoeur.jglm.Matrices
import com.hackoeur.jglm.Vec4
import com.jogamp.common.nio.Buffers
import com.jogamp.graph.curve.opengl.RenderState
import com.jogamp.graph.curve.opengl.TextRenderer
import com.jogamp.graph.font.FontFactory
import com.jogamp.graph.font.FontSet
import com.jogamp.graph.geom.opengl.SVertex
import com.jogamp.opengl.util.Animator
import com.jogamp.opengl.util.glsl.ShaderState
import core.app.ApplicationContext
import core.app.Component
import core.engine.CameraService
import core.engine.Display
import core.engine.EngineCore
import core.engine.LightningService
import core.engine.Point
import core.engine.ScalingService
import core.engine.entity.AlphaIndex
import core.engine.entity.Clickable
import core.engine.entity.CompositeEntity
import core.engine.entity.DynamicEntity
import core.engine.entity.Entity
import core.engine.entity.Rendered
import core.engine.entity.StaticEntity
import core.engine.entity.Text2D
import core.engine.entity.primitive.Primitive2D
import core.engine.entity.primitive.PrimitiveBox2D
import core.engine.loaders.CompositeModelLoader
import core.engine.loaders.ModelLoader
import core.engine.loaders.ObjModelLoader
import core.engine.loaders.PlyModelLoader
import core.engine.loaders.PrimitiveModelLoader
import core.engine.userinput.UserInputListener
import core.jgml.utils.SVec3
import core.jgml.utils.SVec4
import core.opengl.BillboardProgram
import core.opengl.FlatProgram2D
import core.opengl.FlatShaderProgram
import core.opengl.GLProgram
import core.opengl.MainProgram
import core.opengl.MainProgram2D
import core.opengl.OpenGLService
import core.opengl.PickupProgram
import game.core.Lifecycle
import javax.imageio.ImageIO
import javax.media.opengl.DebugGL3
import javax.media.opengl.GL
import javax.media.opengl.GL2ES1
import javax.media.opengl.GL2ES3
import javax.media.opengl.GL2ES2
import javax.media.opengl.GL2GL3
import javax.media.opengl.GL3
import javax.media.opengl.GLAutoDrawable
import javax.media.opengl.GLCapabilities
import javax.media.opengl.GLEventListener
import javax.media.opengl.GLProfile
import javax.media.opengl.GLUniformData
import javax.media.opengl.awt.GLCanvas
import jogamp.graph.curve.opengl.shader.UniformNames
import core.opengl.VertexColorShaderProgram
import scala.collection.immutable.Queue
import core.render.actor.AsyncRenderDataLoader
import core.render.actor.RenderDataRepository
import core.render.texture.InMemoryTexture
import core.utils._
import core.engine.EventBus
import core.engine.messages._

class RenderingCore extends GLEventListener with Component {
  val asyncDataLoader = injectActor[AsyncRenderDataLoader]
  asyncDataLoader.renderingCore = this
  val renderDataRepository = injectActor[RenderDataRepository]
  val eventBus = injectActor[EventBus]

  case class VertexIndexData(vertexData: Array[Float], indexes: Array[Int])

  var lastVboId: Int = -1
  var lastVboPos: Int = 0
  var vboList = List[Int]()
  var freeVbos = List[Int]()
  var vaoList = List[Int]()
  var textureNameToId = Map[String, Int]()
  val boundVaoMesh = collection.mutable.Map[String, RenderData]() // a collection of VAO pointers and indexes lengths based on meshname
  val primitive2dBoxes = collection.mutable.Map[(Int, Int, Int, Int), RenderData]() // a collection of VAO pointes and indexes lenghts based on bx, by, tx, ty for PrimitiveBox2D, 
  // TODO: this is kinda of stupid as if someone start at the same place but has different UV coorods 
  //it would still bind it to the same one
  var mainProgram: GLProgram = _
  var mainProgram2D: GLProgram = _
  var pickupProgram: GLProgram = _
  var flatProgram: GLProgram = _
  var flatProgram2D: GLProgram = _
  var uiElement3dProgram: GLProgram = _
  var vertexColorProgram: GLProgram = _
  val glService = inject[OpenGLService]
  val engineCore = inject[EngineCore]
  val cameraService = inject[CameraService]
  val lightningService = inject[LightningService]
  val userInput = inject[UserInputListener]
  val display = inject[Display]
  var currId = 0;
  var frames = 0;
  var lastMillis = System.currentTimeMillis()
  var swapInterval = 1
  val pickupDivider = 128
  val pickupTh: Float = 1f / pickupDivider
  var cameraRotation: Float = 0.06f;
  var timeSpentRendering: Long = 0
  var timeSpentSimulating: Long = 0
  var lastRender: Long = 0
  var globalMatricesUbo: Int = 0
  var renderedVertices: Long = 0
  var renderer: TextRenderer = null
  var rs: RenderState = null
  var lastFrames = 0;
  private var pause: Option[Promise[() => Future[Unit]]] = None
  private var paused = true // this is used to verify if I should no longer keep rendering
  private var reloadCompleted: Promise[Unit] = _
  private var reloadMeshes = true // I am still pending my first load (reload)
  private var fpsCounter = false
  private var backgroundColor = SVec4(0, 0, 0, 1)

  var dataToLoad: FloatBuffer = _
  var dataToLoadPos: Long = 0
  var entityToBeLoaded: Entity = _
  val BATCH_SIZE = 15000

  def showFpsCounter = fpsCounter = true
  def updateBackgroundColor(color: Vec4) = backgroundColor = color

  /**
   * Ability to pause rendering and load new scene
   * @return future this future gets completed once the rendering succesfuly stopped (happens at the end of the frame), once the promise that the future is completed with is completed it will resume rendering
   */
  def pauseAndSwitch() = {
    val p = promise[() => Future[Unit]]
    val f = p.future
    pause = Some(p)
    f
  }

  def start(): Unit = {
    // initialize whatever from the structural point

    // start up the rendering cycle
    val c = new GLCapabilities(GLProfile.getDefault())
    val samples = 4

    c.setSampleBuffers(true)
    c.setNumSamples(samples)
    c.setAccumAlphaBits(samples)
    c.setAccumBlueBits(samples)
    c.setAccumGreenBits(samples)
    c.setAccumRedBits(samples)
    c.setStencilBits(8)

    val canvas = new GLCanvas(c);
    val frame = new Frame("Asoc");
    val animator = new Animator(canvas);
    animator.setRunAsFastAsPossible(true);
    canvas.addGLEventListener(this);
    frame.add(canvas);
    frame.setSize(640, 480);
    frame.setResizable(true);
    frame.addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent) {
        animator.stop();
        frame.dispose();
        System.exit(0);
      }
    });
    val userInputListener = ApplicationContext.get.getInstance(classOf[UserInputListener])
    frame.addMouseListener(userInputListener)
    frame.setVisible(true);
    canvas.addMouseListener(userInputListener)
    canvas.addMouseMotionListener(userInputListener)
    canvas.addKeyListener(userInputListener)
    canvas.setFocusTraversalKeysEnabled(false)
    animator.start();
    canvas.requestFocus();
  }

  val scalingService = inject[ScalingService]

  /**
   * Handles user input
   */
  def handleUserInput(glDrawable: GLAutoDrawable, gl: GL3) = {
    // setup the camera for pickup (same as prev frame)
    val c = cameraService.camera.getBuffer().array() ++ cameraService.mat.getBuffer().array()
    gl.glBufferSubData(GL2ES3.GL_UNIFORM_BUFFER, 0, 32 * Buffers.SIZEOF_FLOAT, Buffers.newDirectFloatBuffer(c))

    // if something was clicked, render using the pickup shader I need to do this before I handle the frame since it needs to correspond to what user saw on the screen
    for (event <- userInput.eventReceived) {
      userInput.eventReceived = None
      // handle clicks on the ui
      val guid2d = engineCore.entities2d.foldLeft(0) {
        case (agg, e: PrimitiveBox2D with Clickable with Guid with Rendered) if e.rendered && e.inBounds(event.getX(), glDrawable.getHeight() - event.getY()) => e.guid
        case (agg, _) => agg
      }

      val pickedGuid = guid2d match {
        case g if g > 0 => g // we found a 2d guid already so return that
        case _ => // we haven't found 2d guid so go through the pickup program render pass
          // render all the clickable stuff with the pickup program
          pickupProgram.smartEnable(gl, glDrawable)
          engineCore.allEntities foreach {
            case e: Entity with Clickable with Guid => pickupProgram.renderEntity(gl, e)
            case _ =>
          }

          // read the pixel under cursor
          val b = FloatBuffer.allocate(3)
          gl.glReadPixels(event.getX(), glDrawable.getHeight() - event.getY(), 1, 1, GL.GL_RGB, GL.GL_FLOAT, b)
          (b.get(0), b.get(1)) match {
            case (first, second) if first > 0 || second > 0 => // ignore 0, 0 since all my ids start from 1
              /*println(first)
              println(second)
              println((first / pickupTh).toInt * pickupDivider)
              println((second / pickupTh).toInt)*/
              // dirty fix for the rounding error
              val d = second match {
                case n: Float if n > 0.49703925f => (second / pickupTh).toInt + 1
                case _ => (second / pickupTh).toInt
              }
              ((first / pickupTh).toInt * pickupDivider) + d // see pickup program that is where it is being set
            case _ => 0
          }
      }
      // raycast from the x,y coordinates and send the event
      for (ev <- userInput.mouseClicked) {
        val x = (2.0f * ev.getX()) / display.width - 1.0f;
        val y = 1.0f - (2.0f * ev.getY()) / display.height;
        val z = 1.0f;
        val ray_nds = SVec3(x, y, z);
        //println(s"ray_nds: $ray_nds")
        val ray_clip = SVec4(ray_nds.getX(), ray_nds.getY(), -1.0f, 1.0f);
        val ray_eye = Matrices.inverse(cameraService.perspective).multiply(ray_clip)
        val up_ray_eye = SVec4(ray_eye.getX(), ray_eye.getY(), -1.0f, 0.0f);
        val ray_wor = Matrices.inverse(cameraService.camera).multiply(up_ray_eye)
        val normalized_world_ray = SVec3(ray_wor.getX(), ray_wor.getY(), ray_wor.getZ()).getUnitVector()
        // println(s"normalized: $normalized_world_ray")
        //println(s"camera world space: " + cameraService.cameraWorldSpace)
        val clickedEntity: Option[Entity] = pickedGuid match {
          case 0 => None
          case _ =>
            val n: Option[Entity] = None
            engineCore.allEntities.foldLeft(n) {
              case (_, e: Entity with Guid) if e.guid == pickedGuid => Some(e)
              case (e, _) => e
            }
        }
        eventBus.send(Clicked(clickedEntity, ev.getModifiers(), normalized_world_ray))
        userInput.mouseClicked = None
      }

      if (pickedGuid > 0) {
        for (ev <- userInput.mouseClicked) {
          /*userInput.clicked = Some(pickedGuid, ev.getModifiers())
          println(s"clicked: $userInput.clicked")
          userInput.mouseClicked = None
          * 
          */
        }
        for (ev <- userInput.mouseReleased) {
          userInput.released = Some(pickedGuid, ev.getModifiers())
          println(s"released: $userInput.released")
          userInput.mouseReleased = None
        }
        for (ev <- userInput.mousePressed) {
          userInput.pressed = Some(pickedGuid, ev.getModifiers())
          println(s"presssed: $userInput.pressed")
          userInput.mousePressed = None
        }
      } else {
        userInput.mouseClicked = None
        userInput.mouseReleased = None
        userInput.mousePressed = None
      }
      for (ev <- userInput.mouseMoved) {
        userInput.moved = Some(pickedGuid, ev.getModifiers(), ev.getX, ev.getY)
        //println(s"moved: $userInput.moved")
        userInput.mouseMoved = None
      }
    }
  }

  /**
   * --------------------------------------- DISPLAY --------------------------------------------
   */
  def display(glDrawable: GLAutoDrawable) {
    //println(cameraService.center, cameraService.height, cameraService.distance, cameraService.height, cameraService.perspective)
    if (paused) {
      engineCore.handleEventsOnly // still want to handle events (i.e. to handle LoadScene events)
      return
    }
    val gl = glDrawable.getGL().getGL3();
    GLProgram.lastProgramId = -1
    GLProgram.lastVaoId = -1
    GLProgram.currentTexture = ""
    GLProgram.currentAlphaTexture = ""
    GLProgram.cullingEnabled = true
    GLProgram.depthTestEnabled = true

    //gl = gl.getContext().setGL( GLPipelineFactory.create("javax.media.opengl.Debug", null, gl, null) ).asInstanceOf[GL3];
    if (reloadMeshes) {
      userInput.mouseClicked = None
      userInput.mouseReleased = None
      userInput.mousePressed = None
      userInput.eventReceived = None
      loadMeshes(gl)
      reloadMeshes = false
      reloadCompleted success ()
    }

    userInput.mouseDragged match {
      case Some(e) if userInput.lastMovedX >= e.getX() && (e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 =>
        cameraService.rotation -= cameraRotation
        userInput.lastMovedX = e.getX()
        userInput.mouseDragged = None
      case Some(e) if userInput.lastMovedX < e.getX() && (e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 =>
        cameraService.rotation += cameraRotation
        userInput.mouseDragged = None
        userInput.lastMovedX = e.getX()
      case _ =>
    }

    //  framerate
    val currentMillis = System.currentTimeMillis();
    if (currentMillis - lastMillis > 1000) {
      lastMillis = currentMillis
      lastFrames = frames
      frames = 0
    }
    frames += 1
    gl.glEnable(GL.GL_DEPTH_TEST)
    // TODO: if this means a big performance hit revisit in the future
    gl.glClearColor(0, 0.0f, 0.0f, 1)
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);
    gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
    gl.glBindBuffer(GL2ES3.GL_UNIFORM_BUFFER, globalMatricesUbo);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0)

    GLProgram.lastProgramId = -1
    GLProgram.lastVaoId = -1
    GLProgram.currentTexture = ""
    GLProgram.currentAlphaTexture = ""
    GLProgram.cullingEnabled = true
    GLProgram.depthTestEnabled = true
    GLProgram.lastVaoId = -1

    handleUserInput(glDrawable, gl)

    GLProgram.lastProgramId = -1
    GLProgram.lastVaoId = -1
    GLProgram.currentTexture = ""
    GLProgram.currentAlphaTexture = ""
    GLProgram.cullingEnabled = true
    GLProgram.depthTestEnabled = true
    GLProgram.lastVaoId = -1

    if (lastRender > 0)
      timeSpentRendering += System.currentTimeMillis() - lastRender
    val r = System.currentTimeMillis()

    // handle the simulations during the frame
    engineCore.handleFrame

    // load new meshes that were created during frame simulation
    // only if I am not batch loading a mesh at this point
    if (renderDataRepository.ready && dataToLoad == null) {
      val d = renderDataRepository.retrieveFirst
      entityToBeLoaded = d.entity // store current entity, it is unregistered from enginecore and I need to reregister it later (once VBO is loaded)
      prepareVboForLoad(gl)
      loadVertexData(gl, d.entity, VertexIndexData(d.vertexData, d.indexData))
      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0) // unbind the buffer
    } else if (dataToLoad != null) {
      prepareVboForLoad(gl)
      loadVbo(gl, Array[Float]()) // I send in blank array as I am using dataToLoad internally
      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0) // unbind the buffer
    }

    timeSpentSimulating += System.currentTimeMillis() - r
    lastRender = System.currentTimeMillis();
    //println("s: " + timeSpentSimulating + "r: " + timeSpentRendering)

    gl.glClearColor(backgroundColor.getX, backgroundColor.getY, backgroundColor.getZ, backgroundColor.getW)
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);
    gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

    // update the UBO  TODO: conditional only if camera moved
    val ubo = cameraService.camera.getBuffer().array() ++ cameraService.mat.getBuffer().array()
    gl.glBufferSubData(GL2ES3.GL_UNIFORM_BUFFER, 0, 32 * Buffers.SIZEOF_FLOAT, Buffers.newDirectFloatBuffer(ubo))
    GLProgram.renderedVertices = 0

    engineCore.allEntities filter {
      case (e: Guid) => true
      case _ => false
    } sortWith {
      case (e1, e2) =>
        val id = e1 match {
          case e: Guid with AlphaIndex => e.alphaIndex
          case _ => 0
        }
        val id2 = e2 match {
          case e: Guid with AlphaIndex => e.alphaIndex
          case _ => 0
        }
        id < id2
    } foreach { entity =>
      entity match {
        case VertexColorShaderProgram(e) =>
          vertexColorProgram.smartEnable(gl, glDrawable)
          vertexColorProgram.renderEntity(gl, e)
        case FlatProgram2D(e) =>
          flatProgram2D.smartEnable(gl, glDrawable)
          flatProgram2D.renderEntity(gl, e)
        case MainProgram2D(e) =>
          mainProgram2D.smartEnable(gl, glDrawable)
          mainProgram2D.renderEntity(gl, e)
        case BillboardProgram(e) =>
          uiElement3dProgram.smartEnable(gl, glDrawable)
          uiElement3dProgram.renderEntity(gl, e)
          uiElement3dProgram.disableProgram(gl)
        case FlatShaderProgram(e) =>
          flatProgram.smartEnable(gl, glDrawable)
          flatProgram.renderEntity(gl, e)
        case MainProgram(e) =>
          mainProgram.smartEnable(gl, glDrawable)
          mainProgram.renderEntity(gl, e)
        case _ =>
      }
      entity match { // if I have a border, use the values in stencil buffer to render border (GLProgram automatically clears the STENCIL_BUFFER and the default func is GL_ALWAYS
        case ee: Border if ee.borderVisible =>
          val pp = pickupProgram.asInstanceOf[PickupProgram]
          // no more modifying of stencil buffer on stencil and depth pass.
          gl.glStencilMask(0x00);
          // and write actual content to depth and color buffer only at stencil shape locations.
          gl.glStencilFunc(GL.GL_NOTEQUAL, 1, 0xFF); // only pass when I am outside of the counture of the object
          // compute border size based on location on camera
          val p1 = cameraService.posOnScreenFromWorldPos(SVec3(ee.x, ee.y, ee.z))
          val borderSize = p1 match {
            case Point(x, y) if x > 0 && y > 0 =>
              val p2 = cameraService.posOnScreenFromWorldPos(SVec3(ee.x, ee.y + 1, ee.z))
              (p2.y - p1.y).toFloat / 43f match { // arbitrary constants
                case n if n > 0 => n
                case _ => 1f
              }
            case Point(_, _) => 1f
          }
          gl.glLineWidth(ee.borderSize.floatValue() * borderSize)
          gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
          pp.smartEnable(gl, glDrawable)
          val borderColor = ee.borderColor
          pp.renderEntityWithColor(gl, ee, borderColor.getX, borderColor.getY, borderColor.getZ)

          gl.glStencilMask(0xFF); // stencil buffer free to write
          gl.glStencilFunc(GL.GL_ALWAYS, 1, 0xFF); // always pass stencil test
          // gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
          gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
          gl.glDisable(GL.GL_STENCIL_TEST)
        case _ =>
      }
    }

    // do render of 2d elements
    val width = glDrawable.getWidth()
    val height = glDrawable.getHeight()
    val ortho2d = Matrices.ortho(0.0f, width, 0, height, -100, 100)
    val ubo2d = new Mat4(1.0f).getBuffer().array() ++ ortho2d.getBuffer().array()
    gl.glBufferSubData(GL2ES3.GL_UNIFORM_BUFFER, 0, 32 * Buffers.SIZEOF_FLOAT, Buffers.newDirectFloatBuffer(ubo2d))

    gl.glBindVertexArray(0)
    gl.glBindBuffer(GL2ES3.GL_UNIFORM_BUFFER, 0);
    gl.glActiveTexture(GL.GL_TEXTURE0);
    gl.glBindTexture(GL.GL_TEXTURE_2D, 0)
    gl.glActiveTexture(GL.GL_TEXTURE1);
    gl.glBindTexture(GL.GL_TEXTURE_2D, 0)
    gl.glUseProgram(renderer.getShaderState().shaderProgram().program)
    renderer.getShaderState().shaderProgram().useProgram(gl, true)
    renderer.getShaderState().uniform(gl, new GLUniformData(UniformNames.gcu_PMVMatrix, 4, 4, ortho2d.translate(SVec3(5, height - 21, 0)).getBuffer()))
    renderer.setColorStatic(gl, 1, 1, 1)
    if (fpsCounter)
      renderer.drawString3D(gl, FontFactory.get(FontSet.FAMILY_MEDIUM).getDefault(), "FPS: " + lastFrames + " faces: " + GLProgram.renderedVertices, Array(100f, 100f, 100f), 18, Array[Int](400))

    // render all the 2d static texts
    engineCore.entities2d foreach {
      case e: Entity with Text2D =>
        renderer.getShaderState().uniform(gl, new GLUniformData(UniformNames.gcu_PMVMatrix, 4, 4, ortho2d.translate(SVec3(e.x, e.y, 0)).getBuffer()))
        renderer.setColorStatic(gl, e.color.getX, e.color.getY, e.color.getZ)
        e match {
          case e: Rendered if !e.rendered => // do not render 
          case _ => renderer.drawString3D(gl, FontFactory.get(FontSet.FAMILY_MEDIUM).getDefault(), e.text, Array(0, 0, 0), e.size, Array[Int](400))
        }
      case _ =>
    }

    GLProgram.lastVaoId = -1
    gl.glUseProgram(0)
    gl.glBindVertexArray(0);
    gl.glBindBuffer(GL2ES3.GL_UNIFORM_BUFFER, 0);

    pause match {
      case Some(p) =>
        paused = true
        dataToLoad = null // may cause problems, verify if something isn't missing after reload
        userInput.mouseClicked = None
        userInput.mouseReleased = None
        userInput.mousePressed = None
        userInput.eventReceived = None
        val restartCmd: () => Future[Unit] = () => {
          println("Completed")
          pause = None
          paused = false
          reloadMeshes = true
          val restartPromise = Promise[Unit]
          reloadCompleted = restartPromise
          //glDrawable.getAnimator().resume()
          restartPromise.future
        }
        p success restartCmd // once restartCmd gets applied it will start the animator
      case None =>
    }

  }

  def textureId(textureName: String) = textureNameToId(textureName)

  /**
   * Setups the vao data, only works for entities that are Meshes since those should already have the vertex data bound
   */
  def setupVao(entity: Entity with Guid) = {
    entity match {
      case e: Mesh with Guid =>
        boundVaoMesh.get(e.meshName) match {
          case None =>
            engineCore.unregister(e) // unregister so it won't get shown until I am done with loading
            asyncDataLoader.loadVertexData(e)
          case Some(renderData) =>
            e.guid = nextId
            e.renderData = renderData
        }
      /*
      case e: PrimitiveBox2D with Guid =>
        primitive2dBoxes.get(e.bottomLeft._1, e.bottomLeft._2, e.topRight._1, e.topRight._2) match {
          case Some(renderData) =>
            e.guid = nextId
            e.renderData = renderData
          case None => throw new RuntimeException(s"vao for primitve2d box doesn't exist")
        }
        */
      case e =>
        engineCore.unregister(e) // unregister so it won't get shown until I am done with loading
        asyncDataLoader.loadVertexData(e)
    }

  }

  def dispose(glDrawable: GLAutoDrawable) {

  }

  def init(glDrawable: GLAutoDrawable) {
    val gl = glDrawable.getGL().getGL3();
    glDrawable.setGL(new DebugGL3(gl))
    rs = RenderState.createRenderState(new ShaderState(), SVertex.factory())
    renderer = TextRenderer.create(rs, 0)
    renderer.init(gl)

    gl.setSwapInterval(swapInterval)
    gl.glClearColor(0, 0.6f, 0.6f, 1)

    gl.glEnable(GL.GL_CULL_FACE);
    gl.glCullFace(GL.GL_BACK);
    gl.glFrontFace(GL.GL_CCW);

    gl.glEnable(GL.GL_STENCIL_TEST)
    gl.glClearStencil(0)
    gl.glStencilFunc(GL.GL_ALWAYS, 1, 0xFF); // always pass stencil test
    gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE); // replace stencil buffer values to ref=1 if I passed both depth and stencil test
    gl.glStencilMask(0xFF); // stencil buffer free to write

    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glDepthMask(true);
    gl.glDepthFunc(GL.GL_LEQUAL);
    gl.glDepthRange(0, 500f);

    gl.glEnable(GL.GL_BLEND);
    gl.glEnable(GL.GL_LINE_SMOOTH)
    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
    gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

    //gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);

    gl.glReadBuffer(GL.GL_BACK)
    // prepare the UBO for shared matrices
    val b = IntBuffer.allocate(1)
    gl.glGenBuffers(1, b);
    globalMatricesUbo = b.get(0)
    gl.glBindBuffer(GL2ES3.GL_UNIFORM_BUFFER, globalMatricesUbo);
    gl.glBufferData(GL2ES3.GL_UNIFORM_BUFFER, 16 * Buffers.SIZEOF_FLOAT * 2, null, GL.GL_DYNAMIC_DRAW);
    gl.glBindBufferRange(GL2ES3.GL_UNIFORM_BUFFER, 0, globalMatricesUbo, 0, 16 * Buffers.SIZEOF_FLOAT * 2)

    // load shaders
    // normal vert, normal fragment
    // create main program
    mainProgram = new MainProgram
    mainProgram.bindProgram(gl)
    // 2d main program
    mainProgram2D = new MainProgram2D
    mainProgram2D.bindProgram(gl)
    // normal vert shader, flat fragment
    flatProgram = new FlatShaderProgram
    flatProgram.bindProgram(gl)
    // flat3d (billboard) vert, flat frag 
    uiElement3dProgram = new BillboardProgram
    uiElement3dProgram.bindProgram(gl)
    // vertex color, ignores textures and uses vertex color instead
    vertexColorProgram = new VertexColorShaderProgram
    vertexColorProgram.bindProgram(gl)
    // 2d ui programs
    flatProgram2D = new FlatProgram2D
    flatProgram2D.bindProgram(gl)
    // pickup vert & shader
    pickupProgram = new PickupProgram
    pickupProgram.bindProgram(gl)

    loadMeshes(gl); // loads vertex data for all the objects registered with the system
    reloadMeshes = false // initial load behind me no longer want to do a reload
    lightningService.enableDiffuseLight(0.5f, 0.5f, 0.5f)
    paused = false
  }

  def nextId(): Int = {
    currId += 1
    currId
  }

  /**
   * Binds the VBO for loading, reuses last, creates new or reuses freeVbos
   */
  def prepareVboForLoad(gl: GL3): Int = {
    gl.glBindVertexArray(0);
    gl.glDisableVertexAttribArray(0);
    gl.glDisableVertexAttribArray(1);
    gl.glDisableVertexAttribArray(2);
    gl.glDisableVertexAttribArray(3);
    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0)
    if (lastVboId == -1) createAndBindVbo(gl) else {
      println("[RENDER CORE]: rebinding buffer " + lastVboId)
      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, lastVboId)
      lastVboId
    }
  }

  /**
   * Creates new VBO if full and loads the old one with vertex data, returns the newVBoId (or the old if no new was created)
   */
  def createNewVboIfFull(gl: GL3, entity: Entity, data: VertexIndexData) = {
    if (lastVboPos + data.vertexData.size > VBO_SIZE) { // I can't fit the next data into current VBO so load it and create a new one
      val vertBufferObject = createAndBindVbo(gl)
      lastVboPos = 0
    }
    lastVboId // if a new one was created, this is corretly set, if it is still the old one then I don't care
  }

  /**
   * Loads vertexIndexData for given entity, using an appropriate model loader
   */
  def getVertexIndexData(entity: Entity, meshToVaoData: collection.mutable.Map[String, RenderData]): VertexIndexData = { // load the data
    entity match {
      case e: DynamicEntity with Mesh with PlyLoader => loadDataForMesh(meshToVaoData, e, PlyModelLoader("models/" + e.meshName + ".ply"))
      case e: DynamicEntity with Mesh => loadDataForMesh(meshToVaoData, e, ObjModelLoader("models/" + e.meshName + ".obj", 0))
      case e: StaticEntity with Mesh => loadDataForMesh(meshToVaoData, e, ObjModelLoader("models/" + e.meshName + ".obj", 0))
      case e: Primitive2D with Guid =>
        val d = loadDataWithModelLoader(PrimitiveModelLoader(e))
        e match {
          case e: PrimitiveBox2D =>
            primitive2dBoxes += (e.bottomLeft._1, e.bottomLeft._2, e.topRight._1, e.topRight._2) -> e.renderData
          case _ =>
        }
        d
      case e: Guid with CompositeEntity =>
        val modelLoader = new CompositeModelLoader(e)
        loadDataWithModelLoader(modelLoader)
      case _ => throw new RuntimeException("Unknown model loader");
    }
  }

  /**
   * Loads vertex data for a given entity into VBO, if the current VBO would be full after the load, creates a new one
   * Also will bind boundVaoMesh property with new meshes whhen appropriate
   */
  def loadVertexData(gl: GL3, entity: Entity, data: VertexIndexData) = {
    entity.asInstanceOf[Guid].guid = nextId
    createNewVboIfFull(gl, entity, data)
    if (data.vertexData.nonEmpty) { // means something was loaded so I want allocate and bind new vao
      val pointers = IntBuffer.allocate(1);
      // generate the vao object 
      gl.glGenVertexArrays(1, pointers);
      val vaoId = pointers.get(0)
      entity match {
        case e: Mesh with Guid =>
          e.renderData = RenderData(vaoId, data.indexes.length, lastVboId, lastVboPos) // bind the renderData to the entity
          boundVaoMesh(e.meshName) = e.renderData // if the mesh was already bound the vertexData would have been empty
        case e: Guid =>
          e.renderData = RenderData(vaoId, data.indexes.length, lastVboId, lastVboPos) // bind the renderData to the entity
      }
      // bind the vertex data for current entity
      bindVertexData(gl, entity.asInstanceOf[Entity with Guid], data.indexes)
      vaoList = entity.asInstanceOf[Guid].renderData.vaoId :: vaoList
      loadVbo(gl, data.vertexData)
      // move the lastVboPos
      lastVboPos += data.vertexData.size
    }
  }

  /**
   * Loads entities from list into buffers, used when the rendering thread is stopped
   */
  val VBO_SIZE = 4000000
  //val VBO_SIZE = 16259360
  def loadEntitiesUsingList(gl: GL3, entities: List[Entity]) = {
    val start = System.currentTimeMillis()
    // this next thing is a strange bug where without it gl was behaving super strangely throwing out of bounds erros and shit like that
    val buffer = IntBuffer.allocate(1);
    gl.glGenBuffers(1, buffer);

    // load the vbos
    prepareVboForLoad(gl)
    for (entity <- entities if entity.isInstanceOf[Guid]) yield {
      // load the data
      val data = getVertexIndexData(entity, boundVaoMesh)
      loadVertexData(gl, entity, data)
      // load textures if needed
      entity match {
        case e: InMemoryTexture with Guid => loadInMemoryTexture(gl, e, GL.GL_TEXTURE0)
        case e: Textured => loadTexture(gl, e.textureName, GL.GL_TEXTURE0)
        case _ =>
      }
      entity match {
        case e: AlphaTexture => loadTexture(gl, e.alphaTextureName, GL.GL_TEXTURE1)
        case _ =>
      }
    }
    println("Loading of meshes took " + (System.currentTimeMillis() - start))
  }

  /**
   * Creates and binds a VBO, returns the new VBO id, if I have free vbos at my disposal then reuse those
   */
  def createAndBindVbo(gl: GL3): Int = {
    val vertBufferObject = freeVbos match {
      case head :: tail => // there is a free VBO
        println("[RENDERING CORE]: reusing old VBO")
        freeVbos = tail // remove it from free
        head // and use it as my next VBO object
      case _ => // there are no free VBOs, allocate a new one
        println("[RENDERING CORE]: allocating new VBO")
        val buffer = IntBuffer.allocate(1);
        gl.glGenBuffers(1, buffer);
        val vboId = buffer.get(0);
        vboList = vboId :: vboList // add it to list so I know about it
        vboId
    }
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertBufferObject);
    val BLANK_BUFFER = (0 until VBO_SIZE) map (x => x.toFloat)
    gl.glBufferData(GL.GL_ARRAY_BUFFER, VBO_SIZE * Buffers.SIZEOF_FLOAT, null, GL.GL_DYNAMIC_DRAW); // insert blank stuff into buffer, this initializes it to a certains size
    lastVboId = vertBufferObject
    println("bound VBO " + vertBufferObject)
    vertBufferObject
  }

  /**
   * Loads the verticesArray to currently bound VBO and undbinds the VBO object, does so in batches if I am currently in a rendering loop
   */
  def loadVbo(gl: GL3, verticesArray: Array[Float]) = {
    val start = System.currentTimeMillis()
    println("pre buffer loading VBO " + lastVboId)
    val vertices = Buffers.newDirectFloatBuffer(verticesArray)
    println("faces " + vertices.limit() / 11 / 3 + " vertices " + vertices.remaining() / 11)
    // the offset for the next computed in such fashion that if I am loading additional data to previously used VBO the offset starts where I ended last time
    reloadMeshes match { // if I am reloading meshes just load the whole buffer, If I am not, do the load in batches
      case true => gl.glBufferSubData(GL.GL_ARRAY_BUFFER, lastVboPos * Buffers.SIZEOF_FLOAT, vertices.remaining() * Buffers.SIZEOF_FLOAT, vertices) // load the actual data into the buffer, buffer was initialized prior to this function call
      case false if vertices.remaining() <= BATCH_SIZE && dataToLoad == null => // not paused but I fit in the batch size
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, lastVboPos * Buffers.SIZEOF_FLOAT, vertices.remaining() * Buffers.SIZEOF_FLOAT, vertices) // load the actual data into the buffer, buffer was initialized prior to this function call
        println("registering ---------------------------------------- " + entityToBeLoaded)
        engineCore.register(entityToBeLoaded) // register the entity so it gets shown
      case false if vertices.remaining() > BATCH_SIZE && dataToLoad == null => // not paused, exceeds size and I havent started batch loading yet
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, lastVboPos * Buffers.SIZEOF_FLOAT, BATCH_SIZE * Buffers.SIZEOF_FLOAT, vertices) // load the first batch
        dataToLoadPos = lastVboPos + BATCH_SIZE
        dataToLoad = vertices
        dataToLoad.position(dataToLoad.position() + BATCH_SIZE) // move the position
      case false if dataToLoad.remaining() > BATCH_SIZE && dataToLoad != null => // I am in middle of loading data in batches
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, dataToLoadPos * Buffers.SIZEOF_FLOAT, BATCH_SIZE * Buffers.SIZEOF_FLOAT, dataToLoad) // load the first batch
        dataToLoadPos = dataToLoadPos + BATCH_SIZE
        dataToLoad.position(dataToLoad.position() + BATCH_SIZE) // move the position
      case false if dataToLoad.remaining() <= BATCH_SIZE && dataToLoad != null =>
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, dataToLoadPos * Buffers.SIZEOF_FLOAT, dataToLoad.remaining() * Buffers.SIZEOF_FLOAT, dataToLoad) // load the last batch
        dataToLoad = null
        println("registering2 ---------------------------------------- " + entityToBeLoaded)
        engineCore.register(entityToBeLoaded) // register the entity so it gets shown
    }
    println("post buffer: " + (System.currentTimeMillis() - start))
  }

  /**
   * Loads meshes from list, doesn't clear all the buffers
   */
  def loadMeshes(gl: GL3, entities: List[Entity]) = {
    // (vaoId, vaoStart - in vertices, indexes)
    loadEntitiesUsingList(gl, entities)
  }

  /**
   * Loads entities that are registered with engine core, clears all the buffers, reuses old VBOs
   */
  def loadMeshes(gl: GL3) = {
    // TODO: I am not deleting this shit since I am trying to reuse them
    //gl.glDeleteBuffers(vboList.size, IntBuffer.wrap(vboList.toArray))
    gl.glBindVertexArray(0);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0)
    // TODO: when this was getting deleted gl was throwing the subbuffer overflow issue
    //gl.glDeleteVertexArrays(vaoList.toSet.size, IntBuffer.wrap(vaoList.toSet.toArray))
    boundVaoMesh.clear
    primitive2dBoxes.clear
    freeVbos = vboList // I already have some VBOs that were initialized previously so reuse them instead of creating new ones
    vaoList = List[Int]()
    lastVboId = -1
    lastVboPos = 0
    // a collection that holds the (vao reference, start for the VBO object, indexes)
    println("load meshes start")
    loadEntitiesUsingList(gl, engineCore.allEntities)
  }

  /**
   * Loads a texture if it already doesn't exits in the map
   */
  def loadTexture(gl: GL3, textureName: String, textureId: Int) = {
    gl.glActiveTexture(textureId);
    textureNameToId.contains(textureName) match {
      case true =>
      case false =>
        println(s"loading texture $textureName")
        val image = ImageIO.read(new File(textureName))
        val textureIDList = new Array[Int](1)
        gl.glGenTextures(1, textureIDList, 0)
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureIDList(0))
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR)
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR)
        val dataBuffer = image.getRaster.getDataBuffer // image is a java.awt.image.BufferedImage (loaded from a PNG file)
        val buffer: Buffer = dataBuffer match {
          case b: DataBufferByte => ByteBuffer.wrap(b.getData)
          case _ => null
        }
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, image.getWidth, image.getHeight, 0, GL2GL3.GL_BGR, GL.GL_UNSIGNED_BYTE, buffer)
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0)
        textureNameToId += textureName -> textureIDList(0)
    }
  }

  /**
   * Loads a texture if it already doesn't exits in the map
   */
  def loadInMemoryTexture(gl: GL3, entity: InMemoryTexture with Guid, textureId: Int) = {
    gl.glActiveTexture(textureId);
    val textureName = "tex_" + entity.guid
    textureNameToId.contains(textureName) match {
      case true =>
      case false =>
        println(s"loading texture $textureName")
        val textureIDList = new Array[Int](1)
        gl.glGenTextures(1, textureIDList, 0)
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureIDList(0))
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR)
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR)
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, entity.textureWidth, entity.textureHeight, 0, GL.GL_RGB, GL.GL_FLOAT, FloatBuffer.wrap(entity.textureData))
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0)
        textureNameToId += textureName -> textureIDList(0)
    }
  }

  def reshape(glDrawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
    val gl = glDrawable.getGL().getGL3();
    cameraService.setupPerspective(width, height)
    gl.glViewport(0, 0, width, height);
    Lifecycle.screenWidth = width
    Lifecycle.screenHeight = height
    display.width = width
    display.height = height
  }

  private def bindVertexData(gl: javax.media.opengl.GL3, entity: Entity with Guid, indexes: Array[Int]): Unit = {
    val start = System.currentTimeMillis()

    // now for all meshes create element array buffer and bind vertex atrib pointers
    gl.glBindVertexArray(entity.renderData.vaoId);
    val buffer = IntBuffer.allocate(1);
    gl.glGenVertexArrays(1, buffer)
    val indexBufferObject = buffer.get(0); // indexes are per VAO
    val i = Buffers.newDirectIntBuffer(indexes);

    println("binding " + entity.getClass().getSimpleName() + " indexes " + indexes.size + " vboStart " + entity.renderData.vboStart)
    // load the index data
    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
    gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, i.limit() * Buffers.SIZEOF_INT, i, GL.GL_STATIC_DRAW);

    gl.glEnableVertexAttribArray(0);
    gl.glEnableVertexAttribArray(1);
    gl.glEnableVertexAttribArray(2);
    gl.glEnableVertexAttribArray(3);
    gl.glVertexAttribPointer(0, 4, GL.GL_FLOAT, false, 13 * Buffers.SIZEOF_FLOAT, entity.renderData.vboStart * Buffers.SIZEOF_FLOAT); // 4 vertex data, 3 normal data, 4 color data, 2 uv data
    gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 13 * Buffers.SIZEOF_FLOAT, (entity.renderData.vboStart + 4) * Buffers.SIZEOF_FLOAT); // 4 vertex data, 3 normal data, 4 color data, 2 uv data
    gl.glVertexAttribPointer(1, 4, GL.GL_FLOAT, false, 13 * Buffers.SIZEOF_FLOAT, (entity.renderData.vboStart + 7) * Buffers.SIZEOF_FLOAT); // 4 vertex data, 3 normal data, 4 color data, 2 uv data
    gl.glVertexAttribPointer(3, 2, GL.GL_FLOAT, false, 13 * Buffers.SIZEOF_FLOAT, (entity.renderData.vboStart + 11) * Buffers.SIZEOF_FLOAT); // 4 vertex data, 3 normal data, 4 color data, 2 uv data
    gl.glBindVertexArray(0);
    gl.glDisableVertexAttribArray(0);
    gl.glDisableVertexAttribArray(1);
    gl.glDisableVertexAttribArray(2);
    gl.glDisableVertexAttribArray(3);
    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);

    //println("Vertex data loading took " + (System.currentTimeMillis() - start))

  }

  private def loadDataForMesh(meshToVaoData: collection.mutable.Map[String, RenderData], e: Entity with Mesh, modelLoader: ModelLoader): VertexIndexData = {
    // check if we already have data for this mesh
    if (meshToVaoData.contains(e.meshName)) {
      val renderData = meshToVaoData(e.meshName)
      e.renderData = renderData
      VertexIndexData(Array[Float](), Array[Int]()) // this signalizes the top level function that nothing needs to be loaded or bound
    } else {
      println(s"loading mesh $e.meshName")
      val data = modelLoader.loadModel
      println("[RENDERING CORE: mesh data loaded " + e.meshName + " (from model loader)")
      VertexIndexData(data._1, data._2) // and pass the data up so they can be fed to vbo object
    }
  }

  private def loadDataWithModelLoader(modelLoader: ModelLoader): VertexIndexData = {
    val start = System.currentTimeMillis()
    val data = modelLoader.loadModel
    println("Loading of model data took " + (System.currentTimeMillis() - start))
    val ss = System.currentTimeMillis()
    VertexIndexData(data._1, data._2)
  }

}