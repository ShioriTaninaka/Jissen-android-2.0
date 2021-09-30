package com.newit.school_guide.core.extension

import java.util.regex.Pattern

fun String.isEmail(): Boolean {
    if(this.isEmpty()){
        return false
    }
    //return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    val regex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

fun String.isPhoneNumber(): Boolean {
    if(this.isEmpty()){
        return false
    }
    //return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    val regex = "^[+]?[0-9]{10,13}$"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

fun Int.toStringPoint(): String {
    return this.toString() + "pt"
}