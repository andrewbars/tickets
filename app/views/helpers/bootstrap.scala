package views.helpers

object bootstrap {
  import views.html.helper.FieldConstructor
  implicit val fieldConstructor = FieldConstructor(views.html.helpers.bootstrapform.f)
}