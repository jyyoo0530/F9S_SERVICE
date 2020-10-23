package services

import javax.jms.{Connection, MessageConsumer, Session, TextMessage}
import org.apache.activemq.artemis.core.protocol.stomp.{Stomp, StompConnection}
import org.apache.activemq.artemis.jms.client.{ActiveMQConnectionFactory, ActiveMQTopic}


object ArtemisConsumer {
  val connectionFactory: ActiveMQConnectionFactory = new ActiveMQConnectionFactory("tcp://data.freight9.com:61613")
    .setUser("f9s")
    .setPassword("12345678")
  val connection: Connection = connectionFactory.createConnection()
  connection.start()
  val session: Session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

  def receiveMessage(topicAddress: String): Unit = {
    val topic: ActiveMQTopic = new ActiveMQTopic(topicAddress)
    val consumer: MessageConsumer = session.createConsumer(topic)
    val message = consumer.receive(5000)

    System.out.println("RCVD Message: " + message.toString)
  }

}
