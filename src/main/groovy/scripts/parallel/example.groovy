package scripts.parallel

import groovy.transform.Immutable
import groovy.transform.TupleConstructor
import groovyx.gpars.GParsPool

// Lets define a simple message class for our exercise
@Immutable
class Message {
    String body
    String recipient
    String sender
}

def recipient = 'lorem@ipsum.com'
def sender = 'ipsum@ipsum.com'

// create some data for test purpose
def messagesQueue = [
        new Message('Hello', recipient, sender),
        new Message('How are you today?', recipient, sender),
        new Message('Did you receive my previous message?', recipient, sender),
        new Message('Hey, are you there?', recipient, sender),
        new Message('Ok, I will send you another message', recipient, sender),
        new Message('Did you watch this movie yesterday?', recipient, sender),
        new Message('Lorem ipsum dolor sit amet', recipient, sender),
        new Message('Lorem ipsum dolor sit amet, consectetur adipiscing elit', recipient, sender),
        new Message('sed do eiusmod tempor incididunt ut labore et dolore magna', recipient, sender),
        new Message('aliqua. Ut enim ad minim veniam, quis nostrud exercitation', recipient, sender),
        new Message('ullamco laboris nisi ut aliquip ex ea commodo consequat', recipient, sender),
        new Message('Duis aute irure dolor in reprehenderit in voluptate', recipient, sender),
        new Message('velit esse cillum dolore eu fugiat nulla pariatur', recipient, sender),
        new Message('Excepteur sint occaecat cupidatat non proident', recipient, sender),
        new Message('sunt in culpa qui officia deserunt', recipient, sender),
        new Message('mollit anim id est laborum', recipient, sender),
        new Message('Hello', recipient, sender)
]

// Lets define a transport interface which will be responsible for
// sending multiple messages
interface SenderTransport {
    void send(List<Message> messages, Closure send)
}

// Lets do some groovy magic and implement...
// ... parallel transport executor
def parallelExecutionTransport = { List<Message> messages, Closure send ->
    GParsPool.withPool {
        messages.eachParallel send
    }
} as SenderTransport

// ... and good old sequential transport
def sequentialTransport = { List<Message> messages, Closure send ->
    messages.each send
} as SenderTransport


// Now we need a service that will handle this expensive operation of sending
// message to the recipients
@TupleConstructor
class MessageSender {

    SenderTransport transport

    public void send(List<Message> messages) {
        transport.send messages, { msg ->
            //potentially expensive operation
            println "[${Thread.currentThread().name}] sending message: ${msg.body}"
        }
    }
}


// First, lets test sequential approach...
println 'Testing sequential execution:'

def sequentialMessageSender = new MessageSender(sequentialTransport)
sequentialMessageSender.send(messagesQueue)

// After that lets see how gpars will do the same, but in parallel
println 'Testing parallel execution:'

def parallelMessageSender = new MessageSender(parallelExecutionTransport)
parallelMessageSender.send(messagesQueue)

// What I wanted to show you is how you can mix different approaches using
// groovy syntactic sugars and simplicity of gpars library