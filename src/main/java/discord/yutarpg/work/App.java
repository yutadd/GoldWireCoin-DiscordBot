package discord.yutarpg.work;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Random;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import priv.key.Bouncycastle_Secp256k1;
import priv.key.Secp256k1;

/**
 * Hello world!
 *
 */
public class App extends ListenerAdapter {
	static JDABuilder jdabuilder = new JDABuilder();
	static JDA jda;
	static Socket node;
	static HashMap<String, String> id_pub = new HashMap<>();
	static int day = LocalDateTime.now().getDayOfMonth();
	static HashMap<String, BigDecimal> role_income = new HashMap<>();
	static BigDecimal netwall = new BigDecimal(10000);
	/**
	 * 0:確認中
	 * １:ok
	 * 2:no
	 */
	static HashMap<String, Integer> kakunintyu = new HashMap<>();

	public static void main(String[] args) {

		role_income.put("総本部長", new BigDecimal(0.07));
		role_income.put("顧問", new BigDecimal(0.07));
		role_income.put("総本部-執行役員", new BigDecimal(0.06));
		role_income.put("本部長", new BigDecimal(0.05));
		role_income.put("本部-執行役員", new BigDecimal(0.04));
		role_income.put("部長", new BigDecimal(0.03));
		role_income.put("課長", new BigDecimal(0.02));
		role_income.put("職員", new BigDecimal(0.01));
		/*role_income.put("運営OB", 300);
		role_income.put("常連", 200);
		role_income.put("来賓", 100);*/
		File f = new File("wallet.txt");
		try {
			try {
				BufferedReader fr = new BufferedReader(new FileReader(f));
				for (; f.exists();) {
					byte[] priv;
					BigInteger[] pub = new BigInteger[2];
					String line = fr.readLine();
					if (line == null)
						break;
					BigInteger b = new BigInteger(line.split("~")[1], 16);
					priv = b.toByteArray();
					Secp256k1 g = new Secp256k1();
					pub = g.multiply_G(b);
					id_pub.put(line.split("~")[0], pub[0].toString(16) + "0x0a" + pub[1].toString(16));
				}
				fr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			File file = new File("user_fee.txt");
			try {
				try {
					BufferedReader filer = new BufferedReader(new FileReader(file));
					for (; file.exists();) {
						String line = filer.readLine();
						if (line == null)
							break;
						String id = line.split("~")[0];
						String fee = line.split("~")[1];
						id_fee.put(id, fee);
					}
					filer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Socket so = new Socket();
			InetSocketAddress endpoint_ = new InetSocketAddress("localhost", 65261);
			so.connect(endpoint_, 1024);
			so.getOutputStream().write("light\r\n".getBytes());
			node = so;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		try {
			jda = jdabuilder.setToken("Njc4NTQwMzMxMTQ2NDEyMDYy.GywYyP.tg1lB5gb0QxAPKNpMdZadDAh0IZBRM8TcjiIrU")
					.addEventListeners(new App()).setActivity(Activity.playing("%help で取引を開始しましょう！")).build();
			jda.awaitReady();
		} catch (LoginException | InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		new Diary().start();
		Thread th_node = new Thread() {
			@Override
			public void run() {
				InputStream sock_in = null;
				try {
					sock_in = node.getInputStream();
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				InputStreamReader sock_is = new InputStreamReader(sock_in);
				BufferedReader sock_br = new BufferedReader(sock_is);

				for (;;) {
					boolean mitsukatta = false;
					try {
						String s = sock_br.readLine();
						System.out.println(s);
						if (s.startsWith("balance~")) {
							for (MessageChannel mc : jda.getTextChannelsByName(s.split("~")[1], false)) {
								mc.sendMessage(s.split("~")[2] + "がもってる　おかねは" + s.split("~")[3] + "だよ！").queue();
								mitsukatta = true;
							}
							if (!mitsukatta) {
								try {
									User user = jda.getUserByTag(s.split("~")[1]);
									PrivateChannel pc = user.openPrivateChannel().complete();
									pc.sendMessage(s.split("~")[2] + "がもってる　おかねは" + s.split("~")[3] + "だよ！").queue();
								} catch (Exception e) {
									e.printStackTrace();
									System.out.println("チャンネル、DMが見つかりませんでした。");
								}
							}
						} else if (s.endsWith("~ok")) {
							System.out.println("mining size : " + mining.size());
							System.out.println(s);
							MessageAction ma = mining.get(s.split("~")[0]).editMessage(
									"`そうしん　したよ！[ok]\r\n　　＼\r\n　　とりひきが　せいとうだと　みとめられたよ[ok]\r\n　　　　　＼\r\n　　　　まいにんぐ　してるよ！`");
							mining.put(s.split("~")[0], ma.complete());
						} else if (s.endsWith("~denny")) {
							try {
								System.out.println("mining size : " + mining.size());
								System.out.println(s);
								if (mining.containsKey(s.split("~")[0])) {
									mining.get(s.split("~")[0]).editMessage(
											"`そうしん　したよ！[ok]\r\n　　＼\r\n　　とりひきが　みとめられなかったよ[X]\r\n　　　　　＼\r\n　　　　とりひきに しっぱいしたよ...`")
									.queue();
								}
							} catch (Exception e) {
								System.out.println("");
							}
							mining.clear();
						} else if (s.endsWith("~mined")) {
							String st = s.split("~")[0];
							String[] hashs = st.split("0x0d");
							for (String hash : hashs) {
								if (mining.containsKey(hash)) {
									mining.get(hash).editMessage(
											"`そうしん　したよ！[ok]\r\n　　＼\r\n　　とりひきが　せいとうだと　みとめられたよ[ok]\r\n　　　　　＼\r\n　　　　とりひきが　かんりょうしたよ！[ok]`")
									.queue();
									mining.remove(hash);
								}
							}
						}
					} catch (IOException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}
			}
		};
		th_node.start();
	}

	String src_sign = null;

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		System.out.println("受け取り。");
		File f = new File("wallet.txt");
		MessageChannel channel = event.getChannel();
		Message msg = event.getMessage();
		if (kakunintyu.get(event.getAuthor().getId()) != null && kakunintyu.get(event.getAuthor().getId()).equals(0)) {
			if (msg.getContentRaw().equals("y") || msg.getContentRaw().equals("Y")
					|| msg.getContentRaw().equals("yes")) {
				channel.sendMessage("おっけー！").queue();
				kakunintyu.put(event.getAuthor().getId(), 1);
				event.getMember().getRoles();
			} else {
				channel.sendMessage("キャンセル　したよ！").queue();
				kakunintyu.put(event.getAuthor().getId(), 2);
			}
		} else if (msg.getContentRaw().startsWith("%balance")) {
			try {
				if (id_pub.containsKey(event.getAuthor().getId())) {
					try {
						if (msg.getContentRaw().split(" ")[1] != null) {
							try {
								String sa = id_pub.get(msg.getContentRaw().split(" ")[1]);
								String addr = null;
								String[] cmd = msg.getContentRaw().split(" ");
								String S_1 = null;
								if (sa != null) {
									//%balance 72364827346

									node.getOutputStream().write(
											(("balance~" + ((event.getChannel().getType().equals(ChannelType.PRIVATE))
													? event.getAuthor().getAsTag()
															: event.getChannel().getName()) + "~" + sa) + "\r\n").getBytes());
									return;
								} else {
									//%balace @hoge#8763
									//				<@!283764523>
									if (cmd[1].startsWith("<@!")) {
										S_1 = cmd[1].split("<@!")[1].split(">")[0];
									} else if (cmd[1].startsWith("<@&")) {
										S_1 = cmd[1].split("<@&")[1].split(">")[0];
									} else if (cmd[1].startsWith("<@")) {
										S_1 = cmd[1].split("<@")[1].split(">")[0];
									} else if (id_pub.containsKey(cmd[1])) {
										S_1 = cmd[1];
									} else if (cmd[1].contains("0x0a")) {
										addr = cmd[1];
									} else {
										channel.sendMessage("あてさきが　わからないよ！").queue();
										return;
									}
								}
								if (addr == null)
									addr = id_pub.get(S_1);
								node.getOutputStream()
								.write(("balance~" + ((event.getChannel().getType().equals(ChannelType.PRIVATE))
										? event.getAuthor().getAsTag()
												: event.getChannel().getName()) + "~" + addr + "\r\n").getBytes());
							} catch (Exception e) {
								channel.sendMessage("ボスが　ゆうことを　きいてくれない...").queue();
							}
						} else {
							try {
								node.getOutputStream()
								.write(("balance~"
										+ ((event.getChannel().getType().equals(ChannelType.PRIVATE))
												? event.getAuthor().getAsTag()
														: event.getChannel().getName())
										+ "~" + id_pub.get(event.getAuthor().getId()) + "\r\n").getBytes());
							} catch (IOException e) {
								channel.sendMessage("ボスが　ゆうことを　きいてくれない...").queue();
							}
						}
					} catch (Exception e) {
						try {
							node.getOutputStream()
							.write(("balance~"
									+ ((event.getChannel().getType().equals(ChannelType.PRIVATE))
											? event.getAuthor().getAsTag()
													: event.getChannel().getName())
									+ "~" + id_pub.get(event.getAuthor().getId()) + "\r\n").getBytes());
						} catch (IOException en) {
							channel.sendMessage("ボスが　ゆうことを　きいてくれない...").queue();
						}
					}

				} else {
					byte[] priv;
					BigInteger[] pub = new BigInteger[2];
					Secp256k1 g = new Secp256k1();
					BigInteger[] pubK1 = new BigInteger[2];
					Random rn = new Random();
					BigInteger priK = new BigInteger(255, rn);
					pubK1 = g.multiply_G(priK);
					priv = priK.toByteArray();
					pub = pubK1;
					if (priv != null && pub != null) {
						try {
							id_pub.put(event.getAuthor().getId(), pub[0].toString(16) + "0x0a" + pub[1].toString(16));
							pub_priv.put(id_pub.get(event.getAuthor().getId()), new BigInteger(priv).toString(16));
							if (f.exists()) {
								FileWriter fw = new FileWriter(f, true);
								fw.write(event.getAuthor().getId() + "~" + new BigInteger(priv).toString(16));
								fw.close();
								channel.sendMessage("あたらしい　さいふをつくったよ！" + 0.0).queue();
							} else {
								System.out.println("wallet.txt not found");
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				/*EmbedBuilder eb = new EmbedBuilder();
				eb.setImage("https://lohas.nicoseiga.jp/thumb/6855312i?1499437519");
				eb.setTitle("みんみ...");
				channel.sendMessage(eb.build()).queue();*/
			} catch (Exception e) {
				channel.sendMessage("せるりあんが　わいちゃった　ごめんね！" + e.getCause()).queue();
			}
		} else if (msg.getContentRaw().equals("%addr") || msg.getContentRaw().equals("%address")) {
			channel.sendMessage("きみのさいふは" + id_pub.get(event.getAuthor().getId()) + "だよ！").queue();
		} else if (msg.getContentRaw().equals("%help")) {
			String s = "コマンド:gear:：\r\n" +
					"├`%help : ヘルプ`\r\n" +
					"├`%balance : おかねの　のこりだよ！`\r\n" +
					"│　└`%balance <ID> : ほかのフレンズ　の残高だよ！`\r\n" +
					"├`%addr : きみの　アドレスだよ！`\r\n" +
					"│　└`%address : うえとおんなじ！`\r\n" +
					"├`%trade <取引相手(アドレスかメンションを使えるよ)> <amount> <fee> : とりひきを　できるよ！！`\r\n" +
					"│　└`例 : %trade @Gosh#5490 40 0.5`\r\n" +
					"表示に関して:eyes:：\r\n" +
					"├エラーの　ことを　セルリアンって　いうよ！\r\n" +
					"└ウォレットの　ことを　ボスって　よぶよ！\r\n" +
					"運営への報:moneybag:：\r\n" +
					"├やくわりを　もってる　ひとには　１にちに１かい　コインが　ネットワークの　ウォレットから　しはらわれるよ！\r\n" +
					"├ネットワークの　ウォレットの　アドレス :\r\n" +
					" └`77ab79ebd0ab01f013a01908080826e78af2d8975fd765345c3f8de2c1ab7def0x0ad5d9b0dfe42277772cec27caa5a5df440800d0799308d4a592133b56c29a12f1`"
					+
					"作者：狩りごっこが苦手なフレンズ";
			channel.sendMessage(s).queue();
		} else if (msg.getContentRaw().startsWith("%trade")) {
			long time = System.currentTimeMillis();
			String[] cmd = msg.getContentRaw().split(" ");

			String S_1 = null;
			System.out.println(cmd[1]);
			String addr = null;
			boolean ignore = false;
			try {
				if (App.jda.getUserByTag(msg.getContentStripped().split(" ")[1].split("@")[1]) == null) {
					User m = App.jda.getUserByTag(msg.getContentStripped().split(" ")[1].split("@")[1]);
					if (m == null) {
						channel.sendMessage("そのフレンズは　みつけられ　なかったよ...").queue();
						return;
					} else {
						S_1 = m.getId();
					}
				} else {
					channel.sendMessage("そのフレンズは　みつけられ　なかったよ...").queue();
					return;
				}
			} catch (Exception e) {
				try {
					S_1 = cmd[1].split("<@!")[1].split(">")[0];
				} catch (Exception em) {
					try {
						S_1 = cmd[1].split("<@&")[1].split(">")[0];
					} catch (Exception eer) {
						if (id_pub.containsKey(cmd[1])) {
							addr = id_pub.get(cmd[1]);
						} else {
							addr = cmd[1];
						}
					}
				}
			}
			if (addr == null)
				addr = id_pub.get(S_1);
			if (addr == null && !ignore) {
				channel.sendMessage("さいふが　つくられてない　フレンズだよ！").queue();
				return;
			}
			BigDecimal fee = new BigDecimal(0);
			try {
				if (cmd[3] != null) {
					try {
						fee = new BigDecimal(cmd[3]);
					} catch (Exception e) {
						channel.sendMessage("てすうりょうの　かきかたが　ふせいだよ！").queue();
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				channel.sendMessage("てすうりょうの　かきかたが　ふせいだよ").queue();
				return;
			}
			try {
				src_sign = id_pub.get(event.getAuthor().getId()) +

						addr.replace("0x0a", "0x0b") + "0x0c" + new BigDecimal(cmd[2]) +

						fee +
						"" +
						time;
			} catch (Exception e) {
				channel.sendMessage("きんがくの　かきかたが　ふせいだよ").queue();
				return;
			}
			BigInteger[] sign = sign(event.getAuthor().getId(),
					(hash(src_sign).getBytes()));
			String transaction_sum = id_pub.get(event.getAuthor().getId()) +
					"@" + addr.replaceFirst("0x0a", "0x0b") + "0x0c" + new BigDecimal(cmd[2]) +
					"@" + fee +
					"@" + time + "@" + sign[0].toString(16) + "0x0a" + sign[1].toString(16);
			System.out.println(src_sign + "\r\n" + transaction_sum);
			String kakunin = "`送信先→" + addr + "\r\n"
					+ "数量→" + new BigDecimal(cmd[2]) + "`\r\n"
					+ "**一度送信したあとキャンセルすることができません。**\r\n続行しますか？(y/n)";
			channel.sendMessage(kakunin).queue();
			kakunintyu.put(event.getAuthor().getId(), 0);
			Thread th = new Thread() {
				@Override
				public void run() {
					try {
						for (; kakunintyu.get(event.getAuthor().getId()).equals(0);)
							try {
								Thread.sleep(500);
							} catch (Exception e) {
							/*DONOTHING*/}
						;
						if (kakunintyu.get(event.getAuthor().getId()).equals(1)) {

							MessageAction mc = channel.sendMessage(
									"`そうしん　したよ！[ok]\r\n　　＼\r\n　　とりひきが　せいとうかどうかしらべているよ...\r\n　　　　　＼\r\n　　　　まいにんぐ！`");//TODO
							mining.put(transaction_sum, mc.complete());
							System.out.println("格納したとインプット : " + transaction_sum);
							Thread.sleep(1);
							node.getOutputStream().write(
									("disc_transaction~" + ((event.getChannel().getType().equals(ChannelType.PRIVATE))
											? event.getAuthor().getAsTag()
													: event.getChannel().getName()) + "~" + transaction_sum + "\r\n")
									.getBytes());
							node.getOutputStream().write("notice\r\n".getBytes());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			th.start();
		} else if (msg.getContentRaw().startsWith("%check")) {
			for (TextChannel c : jda.getTextChannelsByName("使う_gold-wire_coin", false)) {
				System.out.println(c.getName());
				for (Member m : c.getMembers()) {
					for (net.dv8tion.jda.api.entities.Role r : m.getRoles()) {
						BigDecimal ro = new BigDecimal(0);
						try {
							ro = netwall.multiply(role_income.get(r.getName())).setScale(0, BigDecimal.ROUND_HALF_UP);
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
										"@" + id_pub.get(m.getId()).replaceFirst("0x0a", "0x0b") + "0x0c" + ro +
										"@" + 0.1 +
										"@" + time + "@" + sign[0].toString(16) + "0x0a" + sign[1].toString(16);
								System.out.println(transaction_sum);
								c.sendMessage(transaction_sum.split("@")[0].split("0x0a")[0] + "さんの給料の支払いステータス↓")
								.queue();
								MessageAction mc = c.sendMessage(
										"`そうしん　したよ！[ok]\r\n　　＼\r\n　　とりひきが　せいとうかどうかしらべているよ...\r\n　　　　　＼\r\n　　　　まいにんぐ!`");
								mining.put(transaction_sum, mc.complete());
								System.out.println("格納したとインプット" + transaction_sum.split("@")[0].split("0x0a")[0]);
								Thread.sleep(1);
								node.getOutputStream()
								.write(("disc_transaction~" + c.getName() + "~" + transaction_sum + "\r\n")
										.getBytes());
								node.getOutputStream().write("notice\r\n".getBytes());
							} catch (Exception e) {
							/*do not do anything */}
						}
					}
				}
			}
		} else if (msg.getContentRaw().startsWith("%private")) {
			System.out.println("private");
			Message m = event.getMessage();
			String s = m.getContentRaw();
			String[] cmd = s.split(" ");
			if (cmd[1] != null) {
				TextChannel c = jda.getTextChannelById(681418313938632718l);
				c.sendMessage(cmd[1]).queue();
			}
		}
	}

	static BigInteger[] sign(String s, byte[] hash) {
		//署名用の乱数
		byte[] priv;
		BigInteger priK = new BigInteger(pub_priv.get(id_pub.get(s)).split("~")[0], 16);
		priv = priK.toByteArray();
		Random rn = new Random();
		byte[] ran = new BigInteger(255, rn).toByteArray();
		BigInteger[] sig = Bouncycastle_Secp256k1.sig(hash, priv, ran);
		return sig;
	}

	public static String hash(String arg) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		byte[] hashInBytes = md.digest(arg.getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		for (byte b : hashInBytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}