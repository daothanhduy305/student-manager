package com.ebolo.studentmanager.views.subjects

import com.ebolo.studentmanager.models.SMSubjectModel
import com.ebolo.studentmanager.services.SMServiceCentral
import com.ebolo.studentmanager.services.SMSubjectRefreshRequest
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.paint.Color
import tornadofx.*

class SMNewSubjectView : View() {
    private val subjectModel = SMSubjectModel()
    private val serviceCentral: SMServiceCentral by di()
    private val message: StringProperty = SimpleStringProperty()
    private val messageColor: PojoProperty<Color> = PojoProperty(bean = Color.BLACK, propName = "MessageColor")

    override val root = form {
        fieldset("Thêm môn học mới", labelPosition = Orientation.HORIZONTAL) {
            field("Tên môn") {
                textfield(subjectModel.name).required()
            }

            vbox(spacing = 10, alignment = Pos.CENTER_RIGHT) {
                hbox(spacing = 10) {
                    paddingTop = 10
                    alignment = Pos.CENTER_RIGHT

                    button("Hủy bỏ") {
                        action {
                            close()
                        }
                    }

                    button("Hoàn tất") {
                        action {
                            messageColor.value = Color.BLACK
                            message.value = ""

                            if (subjectModel.validate()) {
                                message.value = "Đang xử lý..."

                                val result = serviceCentral.subjectService.createNewSubject(subjectModel.name.value)

                                if (result.success) {
                                    fire(SMSubjectRefreshRequest)
                                    close()
                                } else {
                                    messageColor.value = Color.RED
                                    message.value = result.errorMessage
                                }
                            }
                        }
                    }
                }

                label(message) {
                    paddingTop = 10

                    textFillProperty().bind(messageColor)
                }
            }
        }
    }
}