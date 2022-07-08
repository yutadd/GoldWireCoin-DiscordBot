package discord.yutarpg.work;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class Diary extends Thread {
	@Override
	public void run() {
		for (;;) {
			LocalDateTime ldt = LocalDateTime.now();
			for (; !Main.node.isConnected();)
				;
			if (ldt.getDayOfMonth() != day) {
				System.out.println("報酬前");
				day = ldt.getDayOfMonth();
				System.out.println("報酬の時間");
				/*報酬の処理*/
				for (TextChannel c : jda.getTextChannelsByName("使う_gold-wire_coin", false)) {
					System.out.println(c.getName());
					for (Member m : c.getMembers()) {
						for (net.dv8tion.jda.api.entities.Role r : m.getRoles()) {
							BigDecimal ro = new BigDecimal(0);
							try {
								ro = netwall.multiply(role_income.get(r.getName())).setScale(0,
										BigDecimal.ROUND_HALF_UP);
								if (ro == null) {
									ro = new BigDecimal(0);
								}
							} catch (Exception e) {
								ro = new BigDecimal(0);
							}
							Long time = System.currentTimeMillis();
							if (ro.compareTo(new BigDecimal(0)) != 0) {
								String transaction_sum = null;
								try {
									String src_sign = id_pub.get(0 + "") +
											id_pub.get(m.getId()).replaceFirst("0x0a", "0x0b") + "0x0c" + ro +
											0.1 +
											"" +
											time;
									BigInteger[] sign = sign(0 + "",
											(hash(src_sign).getBytes()));
									transaction_sum = id_pub.get(0 + "") +
											"@" + id_pub.get(m.getId()).replaceFirst("0x0a", "0x0b") + "0x0c"
											+ ro +
											"@" + 0.1 +
											"@" + time + "@" + sign[0].toString(16) + "0x0a"
											+ sign[1].toString(16);
									System.out.println(transaction_sum);
									c.sendMessage(
											transaction_sum.split("@")[0].split("0x0a")[0] + "いま　こんなかんじだよ！↓")
									.queue();
									MessageAction mc = c.sendMessage(
											"`そうしん　したよ！[ok]\r\n　　＼\r\n　　とりひきが　せいとうかどうかしらべているよ...\r\n　　　　　＼\r\n　　　　まいにんぐ！`");
									mining.put(transaction_sum, mc.complete());
									System.out.println(
											"格納したとインプット" + transaction_sum.split("@")[0].split("0x0a")[0]);
									Thread.sleep(1);
									node.getOutputStream().write(
											("disc_transaction~" + c.getName() + "~" + transaction_sum + "\r\n")
											.getBytes());
									node.getOutputStream().write("notice\r\n".getBytes());
								} catch (Exception e) {
								/*do not do anything */}
							}
						}
					}
				}
			}
			try {
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
