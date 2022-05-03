package com.scmp.mirror.model

/**
 * Created by wooyukit on 03,May,2022
 */

/** The visitor type.
unc=Unclassified
gst=Guest
reg=Registered
sub=Subscribed */
enum class VisitorType(val type: String) {
    Unclassified("unc"),
    Guest("gst"),
    Registered("reg"),
    Subscribed("sub")
}