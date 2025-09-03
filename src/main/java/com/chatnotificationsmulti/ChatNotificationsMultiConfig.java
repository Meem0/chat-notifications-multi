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
		name = "Notification group 1",
		description = "Group 1",
		position = 0
	)
	String sectionGroup1 = "group1";

    @ConfigItem(
        position = 0,
        keyName = "notificationGroup1",
        name = "Notification enabled",
        description = "Enables notifications for group 1.",
        section = sectionGroup1
    )
    default Notification notificationGroup1()
    {
        return Notification.OFF;
    }

	@ConfigItem(
		position = 1,
		keyName = "wordsStringGroup1",
		name = "Highlight words",
		description = "Highlights the following words in chat, separated by commas.",
		section = sectionGroup1
	)
	default String wordsStringGroup1()
	{
		return "";
	}

	@ConfigItem(
		position = 2,
		keyName = "regexStringGroup1",
		name = "Highlight regex",
		description = "Highlights the following regular expressions in chat, one per line.",
		section = sectionGroup1
	)
	default String regexStringGroup1()
	{
		return "";
	}

    @ConfigSection(
        name = "Notification group 2",
        description = "Group 2",
        position = 1
    )
    String sectionGroup2 = "group2";

    @ConfigItem(
        position = 0,
        keyName = "notificationGroup2",
        name = "Notification enabled",
        description = "Enables notifications for group 2.",
        section = sectionGroup2
    )
    default Notification notificationGroup2()
    {
        return Notification.OFF;
    }

    @ConfigItem(
        position = 1,
        keyName = "wordsStringGroup2",
        name = "Highlight words",
        description = "Highlights the following words in chat, separated by commas.",
        section = sectionGroup2
    )
    default String wordsStringGroup2()
    {
        return "";
    }

    @ConfigItem(
        position = 2,
        keyName = "regexStringGroup2",
        name = "Highlight regex",
        description = "Highlights the following regular expressions in chat, one per line.",
        section = sectionGroup2
    )
    default String regexStringGroup2()
    {
        return "";
    }

    @ConfigSection(
        name = "Notification group 3",
        description = "Group 3",
        position = 2
    )
    String sectionGroup3 = "group3";

    @ConfigItem(
        position = 0,
        keyName = "notificationGroup3",
        name = "Notification enabled",
        description = "Enables notifications for group 3.",
        section = sectionGroup3
    )
    default Notification notificationGroup3()
    {
        return Notification.OFF;
    }

    @ConfigItem(
        position = 1,
        keyName = "wordsStringGroup3",
        name = "Highlight words",
        description = "Highlights the following words in chat, separated by commas.",
        section = sectionGroup3
    )
    default String wordsStringGroup3()
    {
        return "";
    }

    @ConfigItem(
        position = 2,
        keyName = "regexStringGroup3",
        name = "Highlight regex",
        description = "Highlights the following regular expressions in chat, one per line.",
        section = sectionGroup3
    )
    default String regexStringGroup3()
    {
        return "";
    }
}
