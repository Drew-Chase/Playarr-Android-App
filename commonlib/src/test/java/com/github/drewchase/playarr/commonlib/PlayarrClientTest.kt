package com.github.drewchase.playarr.commonlib

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PlayarrClientTest {
    @Test
    fun testGetServerInformation(){
        val client = PlayarrClient("https://playarr.dclabs.app")
        val serverInfo = client.getServerInfo()
        println(serverInfo)
        assertNotNull(serverInfo)
        assertEquals("0.2.1-beta", serverInfo.version)
    }
}