package com.chatnotificationsmulti;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Notification;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "ChatNotificationsMulti"
)
public class ChatNotificationsMultiPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ChatNotificationsMultiConfig config;

	@Inject
	private Notifier notifier;

	@Inject
	@Named("runelite.title")
	private String runeliteTitle;

    private static class NotificationGroupInfo
    {
        List<Pattern> highlightPatterns;
        Notification notification;
    }

    private final List<NotificationGroupInfo> notificationGroupInfos = new ArrayList<>();

    @Provides
	ChatNotificationsMultiConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ChatNotificationsMultiConfig.class);
	}

	@Override
	public void startUp()
	{
		updateHighlights();
	}

	@Override
	protected void shutDown()
	{
        notificationGroupInfos.clear();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("chatnotificationsmulti"))
		{
			updateHighlights();
		}
	}

    private NotificationGroupInfo createNotificationGroupInfo(int groupIndex)
    {
        Notification[] configNotifications = {config.notificationGroup1(), config.notificationGroup2(), config.notificationGroup3()};
        String[] configWordsStrings = {config.wordsStringGroup1(), config.wordsStringGroup2(), config.wordsStringGroup3()};
        String[] configRegexStrings = {config.regexStringGroup1(), config.regexStringGroup2(), config.regexStringGroup3()};

        Notification notification = groupIndex < configNotifications.length ? configNotifications[groupIndex] : null;
        String wordsString = groupIndex < configWordsStrings.length ? configWordsStrings[groupIndex] : "";
        String regexString = groupIndex < configRegexStrings.length ? configRegexStrings[groupIndex] : "";

        if (notification == null)
        {
            return null;
        }

        NotificationGroupInfo notificationGroupInfo = new NotificationGroupInfo();
        notificationGroupInfo.notification = notification;
        notificationGroupInfo.highlightPatterns = new ArrayList<>();

        if (!wordsString.trim().isEmpty())
        {
            List<String> items = Text.fromCSV(wordsString);
            String joined = items.stream()
                .map(Text::escapeJagex) // we compare these strings to the raw Jagex ones
                .map(this::quoteAndIgnoreColor) // regex escape and ignore nested colors in the target message
                .collect(Collectors.joining("|"));
            // To match <word> \b doesn't work due to <> not being in \w,
            // so match \b or \s, as well as \A and \z for beginning and end of input respectively
            notificationGroupInfo.highlightPatterns.add(Pattern.compile("(?:\\b|(?<=\\s)|\\A)(?:" + joined + ")(?:\\b|(?=\\s)|\\z)", Pattern.CASE_INSENSITIVE));
        }

        Splitter
            .on("\n")
            .omitEmptyStrings()
            .trimResults()
            .splitToList(regexString).stream()
            .map(ChatNotificationsMultiPlugin::compilePattern)
            .filter(Objects::nonNull)
            .forEach(notificationGroupInfo.highlightPatterns::add);

        return notificationGroupInfo;
    }

	private void updateHighlights()
	{
        notificationGroupInfos.clear();

        for (int i = 0; i < 3; ++i)
        {
            NotificationGroupInfo groupInfo = createNotificationGroupInfo(i);
            notificationGroupInfos.add(groupInfo);
        }
	}

	private static Pattern compilePattern(String pattern)
	{
		try
		{
			return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		}
		catch (PatternSyntaxException ex)
		{
			return null;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		MessageNode messageNode = chatMessage.getMessageNode();

		switch (chatMessage.getType())
		{
			case PRIVATECHATOUT:
			case DIALOG:
			case MESBOX:
				return;
			case MODCHAT:
			case PUBLICCHAT:
			case FRIENDSCHAT:
			case CLAN_CHAT:
			case CLAN_GUEST_CHAT:
			case CLAN_GIM_CHAT:
			case AUTOTYPER:
			case MODAUTOTYPER:
				if (client.getLocalPlayer() != null && Text.toJagexName(Text.removeTags(chatMessage.getName())).equals(client.getLocalPlayer().getName()))
				{
					return;
				}
				break;
			case CONSOLE:
				// Don't notify for notification messages
				if (chatMessage.getName().equals(runeliteTitle))
				{
					return;
				}
				break;
		}

        for (NotificationGroupInfo notificationGroupInfo : notificationGroupInfos)
        {
            if (!notificationGroupInfo.notification.isEnabled())
            {
                continue;
            }

            boolean matchesHighlight = false;
            // Get nodeValue to store and update in between the different pattern passes
            // The messageNode value is only set after all patterns have been processed
            String nodeValue = messageNode.getValue();

            for (Pattern pattern : notificationGroupInfo.highlightPatterns)
            {
                Matcher matcher = pattern.matcher(nodeValue);
                if (!matcher.find())
                {
                    continue;
                }

                StringBuffer stringBuffer = new StringBuffer();

                do
                {
                    final int end = matcher.end();
                    // Determine the ending color by finding the last color tag up to and
                    // including the match.
                    final String closeColor = MoreObjects.firstNonNull(
                        getLastColor(nodeValue.substring(0, end)),
                        "<col" + ChatColorType.NORMAL + '>');
                    // Strip color tags from the highlighted region so that it remains highlighted correctly
                    final String value = stripColor(matcher.group());

                    matcher.appendReplacement(stringBuffer, "<col" + ChatColorType.HIGHLIGHT + '>' + value + closeColor);

                    matchesHighlight = true;
                }
                while (matcher.find());

                // Append stringBuffer with remainder of message and update nodeValue
                matcher.appendTail(stringBuffer);
                nodeValue = stringBuffer.toString();
            }

            if (matchesHighlight)
            {
                sendNotification(notificationGroupInfo.notification, chatMessage);
                messageNode.setValue(nodeValue);
                messageNode.setRuneLiteFormatMessage(messageNode.getValue());
            }
        }
	}

	private void sendNotification(Notification notification, ChatMessage message)
	{
		String name = Text.removeTags(message.getName());
		String sender = message.getSender();
		StringBuilder stringBuilder = new StringBuilder();

		if (!Strings.isNullOrEmpty(sender))
		{
			stringBuilder.append('[').append(sender).append("] ");
		}

		if (!Strings.isNullOrEmpty(name))
		{
			stringBuilder.append(name).append(": ");
		}

		stringBuilder.append(Text.removeTags(message.getMessage()));
		String m = stringBuilder.toString();
		notifier.notify(notification, m);
	}

	private String quoteAndIgnoreColor(String str)
	{
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < str.length(); ++i)
		{
			char c = str.charAt(i);
			stringBuilder.append(Pattern.quote(String.valueOf(c)));
			stringBuilder.append("(?:<col=[^>]*?>)?");
		}

		return stringBuilder.toString();
	}

	/**
	 * Get the last color tag from a string, or null if there was none
	 *
	 * @param str
	 * @return
	 */
	private static String getLastColor(String str)
	{
		int colIdx = str.lastIndexOf("<col=");
		int colEndIdx = str.lastIndexOf("</col>");

		if (colEndIdx > colIdx)
		{
			// ends in a </col> which resets the color to normal
			return "<col" + ChatColorType.NORMAL + ">";
		}

		if (colIdx == -1)
		{
			return null; // no color
		}

		int closeIdx = str.indexOf('>', colIdx);
		if (closeIdx == -1)
		{
			return null; // unclosed col tag
		}

		return str.substring(colIdx, closeIdx + 1); // include the >
	}

	/**
	 * Strip color tags from a string.
	 *
	 * @param str
	 * @return
	 */
	@VisibleForTesting
	static String stripColor(String str)
	{
		return str.replaceAll("(<col=[0-9a-f]+>|</col>)", "");
	}
}
