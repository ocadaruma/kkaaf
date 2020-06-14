package com.mayreh.toyka.example

import java.net.InetSocketAddress
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.nio.{ByteBuffer, ByteOrder}
import java.util.zip.CRC32C

object Application {
  def main(args: Array[String]): Unit = {
    val brokerHost = args(0)
    val brokerPort = args(1).toInt

    val batchBodyBuffer = createBatchBodyBuffer()
    val crc = computeCrc(batchBodyBuffer, 0, batchBodyBuffer.limit())

    val requestBuffer = ByteBuffer.allocate(200).order(ByteOrder.BIG_ENDIAN)

    // size
    requestBuffer.putInt(168)

    // request header
    requestBuffer.putShort(0) // api key
    requestBuffer.putShort(8) // api version
    requestBuffer.putInt(55301) // correlation id

    requestBuffer.putShort("example-manual-produce".length.asInstanceOf[Short])
    requestBuffer.put("example-manual-produce".getBytes(StandardCharsets.UTF_8)) // client id

//    writeUnsignedVarint(0, requestBuffer) // tagged fields

    // produce request body
    requestBuffer.putShort((-1).asInstanceOf[Short]) // transactional id
    requestBuffer.putShort((-1).asInstanceOf[Short]) // acks
    requestBuffer.putInt(3000) // timeout

    requestBuffer.putInt(1) // start of array
    requestBuffer.putShort("example-manual-produce-topic".length.asInstanceOf[Short]) // topic
    requestBuffer.put("example-manual-produce-topic".getBytes(StandardCharsets.UTF_8))

    requestBuffer.putInt(1) // start of array
    requestBuffer.putInt(2) // partition

    requestBuffer.putInt(8 + 4 + 4 + 1 + 4 + batchBodyBuffer.limit()) // records bytes length

    requestBuffer.putLong(0L) // start of first batch // base offset
    requestBuffer.putInt(4 /* partition leader epoch */ + 1 /* magic */ + 4 /* crc */ + batchBodyBuffer.limit()) // size in bytes
    requestBuffer.putInt(-1) // partition leader epoch
    requestBuffer.put(2.asInstanceOf[Byte]) // magic

    requestBuffer.putInt(crc.asInstanceOf[Int]) // crc 32
    requestBuffer.put(batchBodyBuffer)

    val socket = SocketChannel.open(new InetSocketAddress(brokerHost, brokerPort))
    socket.write(requestBuffer.flip())

    Thread.sleep(1000L)
    socket.close()
  }

  private[this] def createBatchBodyBuffer(): ByteBuffer = {
    val buffer = ByteBuffer.allocate(16384).order(ByteOrder.BIG_ENDIAN)

    val now = System.currentTimeMillis()
    buffer.putShort(0) // attributes
    buffer.putInt(0) // last offset delta
    buffer.putLong(now) // first timestamp
    buffer.putLong(now) // max timestamp
    buffer.putLong(-1L) // producer id
    buffer.putShort((-1).asInstanceOf[Short]) // producer epoch
    buffer.putInt(-1) // base sequence

    buffer.putInt(1)

    val recordBuffer = createRecordBuffer()
    writeVarint(recordBuffer.limit(), buffer) // record size in bytes
    buffer.put(recordBuffer)

    buffer.flip()
  }

  private[this] def createRecordBuffer(): ByteBuffer = {
    val buffer = ByteBuffer.allocate(1024).order(ByteOrder.BIG_ENDIAN)

    buffer.put(0.asInstanceOf[Byte]) // attributes
    writeVarlong(0L, buffer) // timestamp delta
    writeVarint(0, buffer) // offset delta
    writeVarint("my-key".length, buffer) // key length
    buffer.put("my-key".getBytes(StandardCharsets.UTF_8)) // key
    writeVarint("my-value".length, buffer) // value length
    buffer.put("my-value".getBytes(StandardCharsets.UTF_8)) // value
    writeVarint(0, buffer) // header length

    buffer.flip()
  }

  private[this] def writeUnsignedVarint(value: Int, buffer: ByteBuffer): Unit = {
    var result = value
    while ((result & 0xffffff80) != 0L) {
      val b = ((value & 0x7f) | 0x80).asInstanceOf[Byte]
      buffer.put(b);
      result >>>= 7;
    }
    buffer.put(result.asInstanceOf[Byte])
  }

  private[this] def writeVarint(value: Int, buffer: ByteBuffer): Unit = {
    writeUnsignedVarint((value << 1) ^ (value >> 31), buffer)
  }

  private[this] def writeVarlong(value: Long, buffer: ByteBuffer): Unit = {
    var v = (value << 1) ^ (value >> 63);
    while ((v & 0xffffffffffffff80L) != 0L) {
      val b = ((v & 0x7f) | 0x80).asInstanceOf[Byte]
      buffer.put(b);
      v >>>= 7;
    }
    buffer.put(v.asInstanceOf[Byte]);
  }

  private[this] def computeCrc(buffer: ByteBuffer, offset: Int, size: Int): Long = {
    val crc = new CRC32C
    crc.update(buffer.array(), buffer.position() + buffer.arrayOffset() + offset, size)
    crc.getValue
  }
}
