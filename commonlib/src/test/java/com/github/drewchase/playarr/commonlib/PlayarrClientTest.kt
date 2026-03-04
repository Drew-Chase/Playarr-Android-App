package com.github.drewchase.playarr.commonlib

import org.junit.Test
import kotlin.test.assertNotNull

class PlayarrClientTest {
    @Test
    fun testGetServerInformation() {
        val client = PlayarrClient("https://playarr.dclabs.app")
        val serverInfo = client.getServerInfo()
        println(serverInfo)
        assertNotNull(serverInfo)
    }

    @Test(expected = Exception::class)
    fun testGetServerInformationFails() {
        val client = PlayarrClient("https://playarr.example.com")
        client.getServerInfo()
    }
}