package com.app.pixelcraft.data.presets

import com.app.pixelcraft.data.model.Quote

object QuotesLibrary {
    val categories = listOf(
        "Motivation",
        "Success",
        "Friendship",
        "Love",
        "Happiness",
        "Wisdom"
    )

    val quotes = listOf(
        // Motivation
        Quote("Believe you can and you're halfway there.", "Theodore Roosevelt", "Motivation"),
        Quote("The only way to do great work is to love what you do.", "Steve Jobs", "Motivation"),
        Quote("Dream big and dare to fail.", "Norman Vaughan", "Motivation"),
        Quote("Action is the foundational key to all success.", "Pablo Picasso", "Motivation"),
        Quote("It always seems impossible until it's done.", "Nelson Mandela", "Motivation"),

        // Success
        Quote("Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill", "Success"),
        Quote("Don't watch the clock; do what it does. Keep going.", "Sam Levenson", "Success"),
        Quote("Opportunities don't happen. You create them.", "Chris Grosser", "Success"),
        Quote("The secret of getting ahead is getting started.", "Mark Twain", "Success"),

        // Friendship
        Quote("A real friend is one who walks in when the rest of the world walks out.", "Walter Winchell", "Friendship"),
        Quote("Friendship is the only cement that will ever hold the world together.", "Woodrow Wilson", "Friendship"),
        Quote("There is nothing on this earth more to be prized than true friendship.", "Thomas Aquinas", "Friendship"),

        // Love
        Quote("Where there is love there is life.", "Mahatma Gandhi", "Love"),
        Quote("Love is composed of a single soul inhabiting two bodies.", "Aristotle", "Love"),
        Quote("The best thing to hold onto in life is each other.", "Audrey Hepburn", "Love"),

        // Happiness
        Quote("Happiness depends upon ourselves.", "Aristotle", "Happiness"),
        Quote("The purpose of our lives is to be happy.", "Dalai Lama", "Happiness"),
        Quote("Spread love everywhere you go. Let no one ever come to you without leaving happier.", "Mother Teresa", "Happiness"),

        // Wisdom
        Quote("Turn your wounds into wisdom.", "Oprah Winfrey", "Wisdom"),
        Quote("Knowing yourself is the beginning of all wisdom.", "Aristotle", "Wisdom"),
        Quote("Silence is a true friend who never betrays.", "Confucius", "Wisdom")
    )
}
