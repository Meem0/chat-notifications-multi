package com.chatnotificationsmulti;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ChatNotificationsMultiPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ChatNotificationsMultiPlugin.class);
		RuneLite.main(args);
	}
}