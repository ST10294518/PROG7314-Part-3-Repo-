package com.winx.app.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val isiZulu = mapOf(
    "Settings" to "Izilungiselelo",
    "Profile" to "Iphrofayela",
    "Language" to "Ulimi",
    "Recently Deleted" to "Okusanda Kususwa",
    "Security" to "Ezokuphepha",
    "Notifications" to "Izaziso",
    "About Us" to "Mayelana Nathi",
    "Log Out" to "Phuma",
    "Save Profile" to "Gcina Iphrofayela",
    "Phone Number" to "Inombolo Yocingo",
    "Date of Birth" to "Usuku Lokuzalwa",
    "Country" to "Izwe",
    "Email Address" to "Ikheli Le-imeyili",
    "Full Name" to "Igama Eligcwele",
    "Username" to "Igama Lomsebenzisi",
    "Email" to "I-imeyili",
    "Password" to "Iphasiwedi",
    "Confirm Password" to "Qinisekisa Iphasiwedi",
    "Login" to "Ngena",
    "Create Account" to "Dala I-akhawunti",
    "Welcome Back !!" to "Siyakwamukela Futhi !!",
    "Welcome Back" to "Siyakwamukela Futhi",
    "Create your account" to "Dala i-akhawunti yakho",
    "Good Morning Mia!" to "Sawubona!",
    "Add Entry" to "Engeza Okufakiwe",
    "Add Country" to "Engeza Izwe",
    "Calendar" to "Ikhalenda",
    "Entries" to "Okufakiwe",
    "My Entries" to "Okufakiwe Zami",
    "Your travel memories" to "Izinkumbulo zakho zokuhamba",
    "No entries yet" to "Akukho okufakiwe okwamanje",
    "Start capturing your journey by creating your first entry." to "Qala uhambo lwakho ngokudala okufakiwe kwakho kokuqala.",
    "View All" to "Buka Konke",
    "Recent Notifications" to "Izaziso Zakamuva",
    "Stay Updated" to "Hlala Unolwazi",
    "Recent Dishes" to "Izitsha Zakamuva",
    "Save Language" to "Gcina Ulimi",
    "Change App Language" to "Shintsha Ulimi Lohlelo",
    "Select the language you would like to use throughout the app." to "Khetha ulimi ofuna ukulusebenzisa kulo lonke uhlelo.",
    "Language preference saved." to "Okukhethwa kukho kolimi kugciniwe.",
    "Profile saved successfully." to "Iphrofayela igcinwe ngempumelelo.",
    "Manage your Winx preferences." to "Phatha okuncamelayo kwe-Winx.",
    "View and manage your personal information." to "Buka futhi uphathe imininingwane yakho siqu.",
    "Forgot Password?" to "Ukhohlwe Iphasiwedi?",
    "OR" to "NOMA",
    "Capture places. Cherish moments.\nRemember every journey." to "Thwebula izindawo. Gcina izikhathi.\nKhumbula lonke uhambo.",
    "Every Place has a story,\nwhat will you capture today??" to "Yonke indawo inendaba,\nuzothwebula ini namuhla??"
)

private val afrikaans = mapOf(
    "Settings" to "Instellings",
    "Profile" to "Profiel",
    "Language" to "Taal",
    "Recently Deleted" to "Onlangs Verwyder",
    "Security" to "Sekuriteit",
    "Notifications" to "Kennisgewings",
    "About Us" to "Oor Ons",
    "Log Out" to "Teken Uit",
    "Save Profile" to "Stoor Profiel",
    "Phone Number" to "Telefoonnommer",
    "Date of Birth" to "Geboortedatum",
    "Country" to "Land",
    "Email Address" to "E-posadres",
    "Full Name" to "Volle Naam",
    "Username" to "Gebruikersnaam",
    "Email" to "E-pos",
    "Password" to "Wagwoord",
    "Confirm Password" to "Bevestig Wagwoord",
    "Login" to "Teken In",
    "Create Account" to "Skep Rekening",
    "Welcome Back !!" to "Welkom Terug !!",
    "Welcome Back" to "Welkom Terug",
    "Create your account" to "Skep jou rekening",
    "Good Morning Mia!" to "Goeiemôre!",
    "Add Entry" to "Voeg Inskrywing By",
    "Add Country" to "Voeg Land By",
    "Calendar" to "Kalender",
    "Entries" to "Inskrywings",
    "My Entries" to "My Inskrywings",
    "Your travel memories" to "Jou reisherinneringe",
    "No entries yet" to "Nog geen inskrywings nie",
    "Start capturing your journey by creating your first entry." to "Begin jou reis deur jou eerste inskrywing te skep.",
    "View All" to "Bekyk Alles",
    "Recent Notifications" to "Onlangse Kennisgewings",
    "Stay Updated" to "Bly Opgedateer",
    "Recent Dishes" to "Onlangse Geregte",
    "Save Language" to "Stoor Taal",
    "Change App Language" to "Verander Programtaal",
    "Select the language you would like to use throughout the app." to "Kies die taal wat jy deur die toepassing wil gebruik.",
    "Language preference saved." to "Taalvoorkeur gestoor.",
    "Profile saved successfully." to "Profiel suksesvol gestoor.",
    "Manage your Winx preferences." to "Bestuur jou Winx-voorkeure.",
    "View and manage your personal information." to "Bekyk en bestuur jou persoonlike inligting.",
    "Forgot Password?" to "Wagwoord Vergeet?",
    "OR" to "OF",
    "Capture places. Cherish moments.\nRemember every journey." to "Vang plekke vas. Koester oomblikke.\nOnthou elke reis.",
    "Every Place has a story,\nwhat will you capture today??" to "Elke plek het 'n storie,\nwat gaan jy vandag vasvang??"
)

fun translate(text: String, language: String): String {
    return when (language) {
        "isiZulu (Zulu)" -> isiZulu[text] ?: text
        "Afrikaans" -> afrikaans[text] ?: text
        else -> text
    }
}

@Composable
fun T(text: String): String {
    val context = LocalContext.current
    val language = context
        .getSharedPreferences("winx_settings", Context.MODE_PRIVATE)
        .getString("language", "English (US)") ?: "English (US)"

    return translate(text, language)
}

@Composable
fun winxGreeting(name: String): String {
    val context = LocalContext.current
    val language = context
        .getSharedPreferences("winx_settings", Context.MODE_PRIVATE)
        .getString("language", "English (US)") ?: "English (US)"

    return when (language) {
        "isiZulu (Zulu)" -> "Sawubona $name!"
        "Afrikaans" -> "Goeiemôre $name!"
        else -> "Good Morning $name!"
    }
}
