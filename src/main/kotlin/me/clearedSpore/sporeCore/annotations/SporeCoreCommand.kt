package me.clearedSpore.sporeCore.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SporeCoreCommand(val configBoolean: Boolean = true)

