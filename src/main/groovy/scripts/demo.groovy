package scripts

import static groovyx.gpars.actor.Actors.actor

// Case classes ftw? :-)
final class Message {
    private String body
}
final class Noop {}
final class Terminate {}

def listener = actor {
    loop {
        react { message ->
            switch (message) {
                case Noop:
                    println 'Noop arrived, there is nothing to do...'
                    break

                case Message:
                    reply "Message: ${message.body.toUpperCase()}"
                    break

                case Terminate:
                    stop()
                    break
            }
        }
    }
}

def noop = actor {
    listener << new Noop()
}

def message = actor {
    listener << new Message(body: 'Lorem ipsum dolor sit amet')

    react { message ->
        println 'New message arrived...'
        println message
        listener << new Terminate()
    }
}


[listener, noop, message]*.join()