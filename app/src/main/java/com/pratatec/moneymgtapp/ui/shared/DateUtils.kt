package com.pratatec.moneymgtapp.ui.shared

import java.util.Calendar

fun Double.toBRL(): String = "R$ ${"%.2f".format(this).replace(".", ",")}"

fun diasNoMes(mes: Int, ano: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(ano, mes - 1, 1)
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun diaDaSemana(ano: Int, mes: Int, dia: Int): String {
    val cal = Calendar.getInstance()
    cal.set(ano, mes - 1, dia)
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "Dom"
        Calendar.MONDAY -> "Seg"
        Calendar.TUESDAY -> "Ter"
        Calendar.WEDNESDAY -> "Qua"
        Calendar.THURSDAY -> "Qui"
        Calendar.FRIDAY -> "Sex"
        Calendar.SATURDAY -> "Sáb"
        else -> ""
    }
}

fun nomeMes(mes: Int): String = when (mes) {
    1 -> "Janeiro"; 2 -> "Fevereiro"; 3 -> "Março"; 4 -> "Abril"
    5 -> "Maio"; 6 -> "Junho"; 7 -> "Julho"; 8 -> "Agosto"
    9 -> "Setembro"; 10 -> "Outubro"; 11 -> "Novembro"; 12 -> "Dezembro"
    else -> ""
}

fun nomeMesAbrev(mes: Int): String = when (mes) {
    1 -> "jan"; 2 -> "fev"; 3 -> "mar"; 4 -> "abr"
    5 -> "mai"; 6 -> "jun"; 7 -> "jul"; 8 -> "ago"
    9 -> "set"; 10 -> "out"; 11 -> "nov"; 12 -> "dez"
    else -> ""
}

fun formatDateShort(dateStr: String): String {
    val parts = dateStr.split("-")
    if (parts.size != 3) return dateStr
    val ano = parts[0].toIntOrNull() ?: return dateStr
    val mes = parts[1].toIntOrNull() ?: return dateStr
    val dia = parts[2].toIntOrNull() ?: return dateStr
    return "$dia ${nomeMesAbrev(mes)} ${diaDaSemana(ano, mes, dia)}"
}
