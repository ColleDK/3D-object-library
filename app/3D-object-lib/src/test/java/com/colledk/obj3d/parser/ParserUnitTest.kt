package com.colledk.obj3d.parser

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ParserUnitTest {

    @Test
    fun parseFileVertices(){
        val stream = MockedParserData.objectDataString.byteInputStream()

        val data = runBlocking {
            ObjectFileParser().parseStream(inputStream = stream, warnOnThreshold = true)
        }

        assert(data.vertices == MockedParserData.parsedData.vertices)
    }

    @Test
    fun parseFileFaces(){
        val stream = MockedParserData.objectDataString.byteInputStream()

        val data = runBlocking {
            ObjectFileParser().parseStream(inputStream = stream, warnOnThreshold = true)
        }

        assert(data.faces == MockedParserData.parsedData.faces)
    }
}