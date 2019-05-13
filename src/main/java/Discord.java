import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import java.util.List;

public class Discord implements EventListener {

    String token;

    Discord(String token) {
        this.token = token;
    }

    public void onEvent(Event event) {
        if (event instanceof MessageReceivedEvent) act((MessageReceivedEvent) event);
    }

    private void act(MessageReceivedEvent event) {
        if (event.getChannel().getName().equals("ssh-plex-51")) {
            if (!event.getAuthor().isBot() && !Main.pendingResponse) {
                event.getChannel().sendMessage("Response inc").queue();
                Main.sendMessage(event.getMessage().getContentRaw() + "\n");
            }
        }
    }


    /**
     * Should fix only when a person sends Clear
     *
     * @param event
     */
    private void deleteStuff(MessageReceivedEvent event) {
        System.out.println("it is supposed to clear now");
        List<Message> hallo = event.getTextChannel().getHistory().retrievePast(100).complete();
        System.out.println(hallo.size());
        hallo.forEach(message -> {
            message.delete().queue();
        });


    }

    String getToken() {
        return token;
    }
}
