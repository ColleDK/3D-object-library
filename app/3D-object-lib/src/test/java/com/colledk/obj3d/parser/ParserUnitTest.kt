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
            ObjectFileParser().parseStream(stream, onFinish = {})
        }

        assert(data.vertices == MockedParserData.parsedData.vertices)
        //assert(data.vertices.size == 8)
        //assert(data.vertices.containsAll(MockedParserData.parsedData.vertices) && MockedParserData.parsedData.vertices.containsAll(data.vertices))
    }

    @Test
    fun parseFileFaces(){
        val stream = MockedParserData.objectDataString.byteInputStream()

        val data = runBlocking {
            ObjectFileParser().parseStream(stream, onFinish = {})
        }

        assert(data.faces == MockedParserData.parsedData.faces)
        //assert(data.faces.size == 12)
        //assert(data.faces.containsAll(MockedParserData.parsedData.faces) && MockedParserData.parsedData.faces.containsAll(data.faces))
    }
}