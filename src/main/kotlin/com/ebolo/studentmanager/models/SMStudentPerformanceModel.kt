package com.ebolo.studentmanager.models

import com.ebolo.studentmanager.entities.SMStudentPerformanceInfo
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import tornadofx.ItemViewModel
import tornadofx.getProperty
import tornadofx.isInt
import tornadofx.property
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Class served as the item view model for the student performance info
 *
 * @author ebolo
 * @since 0.0.1-SNAPSHOT
 */
class SMStudentPerformanceModel(
    item: SMStudentPerformanceDto
) : ItemViewModel<SMStudentPerformanceModel.SMStudentPerformanceDto>(item) {

    // region dto
    class SMStudentPerformanceDto(val performanceInfo: SMStudentPerformanceInfo) {
        var note by property<String>()
        fun noteProperty() = getProperty(SMStudentPerformanceDto::note)

        var startDate by property<LocalDate>()
        fun startDateProperty() = getProperty(SMStudentPerformanceDto::startDate)

        val resultsPropertyList: MutableList<StringProperty> = performanceInfo
            .results
            .map { SimpleStringProperty(if (it > -1) it.toString() else "") }
            .toMutableList()

        init {
            startDateProperty().value = performanceInfo.startDate?.atOffset(ZoneOffset.UTC)?.toLocalDate()
            noteProperty().value = performanceInfo.note
        }
    }
    // endregion

    // region bindings
    val note = bind(SMStudentPerformanceDto::noteProperty)
    val startDate = bind(SMStudentPerformanceDto::startDateProperty)
    // endregion

    fun toEntity() = SMStudentPerformanceInfo(
        student = item.performanceInfo.student,
        note = note.value,
        startDate = startDate.value?.atStartOfDay()?.toInstant(ZoneOffset.UTC),
        results = item.resultsPropertyList.map {
            if (!it.value.isNullOrBlank() && it.value.isInt() && it.value.toInt() > -1) {
                it.value.toInt()
            } else {
                -1
            }
        }.toMutableList()
    )
}