package xen.commands.config;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import xen.lib.Utils;
import xen.lib.command.Command;
import xen.lib.command.CommandContext;
import xen.lib.mongodb.guild.GuildModel;

import java.util.concurrent.atomic.AtomicReference;

public class WelcomeChannel extends Command {
  private GuildModel guildModel;

  public WelcomeChannel() {
    super("welcomeChannel");
    setCategory(Categories.CONFIG);
    setDescription("Let's you set a channel for member join logs.");
    setUsage("[Channel Mention | ID] - Leave it blank to disable join logs");
    setPerms(new Permission[]{Permission.ADMINISTRATOR});
  }

  @Override
  public void run(@NotNull CommandContext ctx) {
    guildModel = (GuildModel) ctx.getClient().getDbManager().find(ctx.getEvent().getGuild());

    if (ctx.getArgs().isEmpty()) {
      guildModel.getWelcome().setJoins("");
      ctx.getClient().getDbManager().save(guildModel);
      Utils.sendEm(
              ctx.getEvent().getChannel(),
              ctx.getClient().getTick() + " Join logs have been disabled!",
              Utils.Embeds.SUCCESS
      ).queue();
      return;
    }

    AtomicReference<TextChannel> channel = new AtomicReference<>();
    if (
            !ctx.getEvent().getMessage().getMentionedChannels().isEmpty() &&
                    ctx.getEvent().getGuild().getTextChannels().contains(
                            ctx.getEvent().getMessage().getMentionedChannels().get(0)
                    )
    )
      channel.set(ctx.getEvent().getMessage().getMentionedChannels().get(0));
    if (Utils.isSnowflake(ctx.getArgs().get(0)))
      channel.set(ctx.getEvent().getGuild().getTextChannelById(ctx.getArgs().get(0)));
    if (channel.get() == null) {
      Utils.sendEm(
              ctx.getEvent().getChannel(),
              ctx.getClient().getCross() + " No channel found with the given info!",
              Utils.Embeds.ERROR
      ).queue();
      return;
    }

    guildModel.getWelcome().setJoins(channel.get().getId());
    ctx.getClient().getDbManager().save(guildModel);

    Utils.sendEm(
            ctx.getEvent().getChannel(),
            ctx.getClient().getTick() + " Join channel changed to " +
                    channel.get().getAsMention() + "!",
            Utils.Embeds.SUCCESS
    ).queue();
    Utils.sendConfigLog(
            ctx.getEvent(),
            guildModel,
            "Changed Joins Channel",
            "Joins log channel changed to " + channel.get().getAsMention() +
                    " | " + channel.get().getId()
    );
  }
}