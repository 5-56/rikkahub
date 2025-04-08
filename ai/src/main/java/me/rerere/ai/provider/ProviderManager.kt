package me.rerere.ai.provider

import me.rerere.ai.provider.providers.OpenAIProvider

/**
 * Provider管理器，负责注册和获取Provider实例
 */
object ProviderManager {
    // 存储已注册的Provider实例
    private val providers = mutableMapOf<String, Provider<*>>()
    
    init {
        // 注册默认Provider
        registerProvider("openai", OpenAIProvider)
    }
    
    /**
     * 注册Provider实例
     * 
     * @param name Provider名称
     * @param provider Provider实例
     */
    fun registerProvider(name: String, provider: Provider<*>) {
        providers[name] = provider
    }
    
    /**
     * 获取Provider实例
     * 
     * @param name Provider名称
     * @return Provider实例，如果不存在则返回null
     */
    fun getProvider(name: String): Provider<*> {
        return providers[name] ?: throw IllegalArgumentException("Provider not found: $name")
    }
    
    /**
     * 根据ProviderSetting获取对应的Provider实例
     * 
     * @param setting Provider设置
     * @return Provider实例，如果不存在则返回null
     */
    fun getProviderByType(setting: ProviderSetting): Provider<*> {
        return when (setting) {
            is ProviderSetting.OpenAI -> getProvider("openai")
            is ProviderSetting.Google -> getProvider("google")
        }
    }
}