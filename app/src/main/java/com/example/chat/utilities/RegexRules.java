package com.example.chat.utilities;

import java.util.regex.Pattern;

public class RegexRules {
    public static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\w+([.-]?\\w+)" +             // any letter, number underscore or dash
                            "*@(hotmail|outlook|yahoo|gmail)" +   // only hotmail, outlook, yahoo or gmail
                            "\\.(com)" +                          // ends only with dot and com (.com)
                            "$"
                    ,Pattern.CASE_INSENSITIVE);

    public static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9]{4,})" +     // at least 4 digit
                    "(?=.*[a-z])" +         // at least 1 lower case letter
                    "(?=.*[A-Z])" +         // at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      // any letter
                    "(?=.*[@#$%^&+=])" +    // at least 1 special character
                    "(?=\\S+$)" +           // no white spaces
                    ".{4,}" +               // at least 4 characters
                    "$");
    public static final Pattern NAME_FORM =
            Pattern.compile("^" +
                    "[a-zA-z[-']?]{2,}" +   // letters , dash or single quote
                    "+\\s+" +               // white space
                    "[a-zA-z[-']?]{2,}" +   // letters , dash or single quote
                    "$");

    public static final Pattern Empty = Pattern.compile("^" +
            "\\S+(?: \\S+)*" +
            "$");

}
