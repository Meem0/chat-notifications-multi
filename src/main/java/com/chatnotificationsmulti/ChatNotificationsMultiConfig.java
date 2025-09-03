package com.chatnotificationsmulti;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Notification;

@ConfigGroup("chatnotificationsmulti")
public interface ChatNotificationsMultiConfig extends Config
{
	@ConfigSection(
		name = "Highlight lists",
		description = "Custom single word and regex filter lists.",
		position = 0
	)
	String highlightLists = "highlightLists";

	@ConfigItem(
		position = 1,
		keyName = "highlightWordsString",
		name = "Highlight words",
		description = "Highlights the following words in chat, separated by commas.",
		section = highlightLists
	)
	default String highlightWordsString()
	{
		return "";
	}

	@ConfigItem(
		position = 2,
		keyName = "highlightRegexString",
		name = "Highlight regex",
		description = "Highlights the following regular expressions in chat, one per line.",
		section = highlightLists
	)
	default String highlightRegexString()
	{
		return "";
	}

	@ConfigItem(
		position = 1,
		keyName = "notifyOnHighlight",
		name = "Notify on highlight",
		description = "Notifies you whenever a highlighted word is matched."
	)
	default Notification notifyOnHighlight()
	{
		return Notification.OFF;
	}
}
