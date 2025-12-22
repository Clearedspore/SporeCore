package me.clearedSpore.sporeCore.features.chat.channel.`object`


data class Channel(
    var name: String,
    var id: String,
    var permission: String,
    var prefix: String,
    var message: String,
    var commands: List<String>,
    var symbol: String
)
