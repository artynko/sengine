package core.app

trait RegisteringComponent extends Component {
  ApplicationContext.get.register(this)

}