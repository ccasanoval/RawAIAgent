package com.cesoft.rawagent

import android.app.Application

// AI Agents theory
//https://www.anthropic.com/engineering/building-effective-agents

// AI Agent Python
//https://github.com/daveebbelaar/ai-cookbook/tree/main/patterns/workflows

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        ThisApp = this
    }
    companion object {
        lateinit var ThisApp: App
    }
}