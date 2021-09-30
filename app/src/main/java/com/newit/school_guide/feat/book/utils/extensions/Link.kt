package com.newit.school_guide.feat.book.utils.extensions

import org.readium.r2.shared.publication.Link

val Link.outlineTitle: String
    get() = title ?: href
