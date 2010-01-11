package jp.sourceforge.andjong.mahjong;

import android.util.Log;

public class Man implements EventIf {
	/** プレイヤーに提供する情報 */
	private Info m_info;

	/** 捨牌のインデックス */
	private int m_iSutehai = 0;

	private String name;

	public String getName() {
		return name;
	}

	private PlayerAction m_playerAction;

	public Man(Info info, String name, PlayerAction playerAction) {
		this.m_info = info;
		this.name = name;
		this.m_playerAction = playerAction;
	}

	/** 手牌 */
	private Tehai m_tehai = new Tehai();

	@Override
	public EventId event(EventId eid, int a_kazeFrom, int a_kazeTo) {
		int sutehaiIdx = 0;
		int agariScore = 0;
		Hai tsumoHai;
		Hai suteHai;
		int indexNum = 0;
		int[] indexs = new int[14];
		int menuNum = 0;
		EventId eventId[] = new EventId[4];
		int jyunTehaiLength;

		Hai[] kanHais = new Hai[4];
		int kanNum = 0;
		Hai[] sarashiHaiLeft = new Hai[2];
		Hai[] sarashiHaiCenter = new Hai[2];
		Hai[] sarashiHaiRight = new Hai[2];
		//boolean isChii = false;
		int chiiCount = 0;
		int iChii = 0;
		int relation = a_kazeFrom - a_kazeTo;

		switch (eid) {
		case TSUMO:
			// 手牌をコピーする。
			m_info.copyTehai(m_tehai, m_info.getJikaze());
			// ツモ牌を取得する。
			tsumoHai = m_info.getTsumoHai();

			if (!m_info.isReach()) {
				Log.d("Man", "getReachIndexs in");
				indexNum = m_info.getReachIndexs(m_tehai, tsumoHai, indexs);
				Log.d("Man", "getReachIndexs = " + indexNum);
				if (indexNum > 0) {
					m_playerAction.setValidReach(true);
					eventId[menuNum] = EventId.REACH;
					menuNum++;
				}
			}

			agariScore = m_info.getAgariScore(m_tehai, tsumoHai);
			if (agariScore > 0) {
				Log.d("Man", "agariScore = " + agariScore);
				m_playerAction.setValidTsumo(true);
				eventId[menuNum] = EventId.TSUMO_AGARI;
				menuNum++;
			}

			kanNum = m_tehai.validKan(tsumoHai, kanHais);
			if (kanNum > 0) {
				m_playerAction.setValidKan(true, kanHais, kanNum);
				eventId[menuNum] = EventId.ANKAN;
				menuNum++;
			}

//			if (menuNum > 0) {
//				m_playerAction.setMenuSelect(5);
//				m_playerAction.setState(PlayerAction.STATE_TSUMO_SELECT);
//				m_info.postUiEvent(EventId.UI_INPUT_PLAYER_ACTION, a_kazeFrom, a_kazeTo);
//				m_playerAction.actionWait();
//				int menuSelect = m_playerAction.getMenuSelect();
//				if ((menuSelect >= 0) && (menuSelect < menuNum)) {
//					m_playerAction.init();
//					if (eventId[menuSelect] == EventId.REACH) {
//						m_playerAction.m_indexs = indexs;
//						m_playerAction.m_indexNum = indexNum;
//						while (true) {
//							// 入力を待つ。
//							m_playerAction.setState(PlayerAction.STATE_SUTEHAI_SELECT);
//							m_playerAction.actionWait();
//							sutehaiIdx = m_playerAction.getSutehaiIdx();
//							if (sutehaiIdx != Integer.MAX_VALUE) {
//								if (sutehaiIdx >= 0 && sutehaiIdx <= 13) {
//									break;
//								}
//							}
//						}
//						m_playerAction.init();
//						this.m_iSutehai = sutehaiIdx;
//					}
//					return eventId[menuSelect];
//				}
//				m_playerAction.init();
//			}

			while (true) {
				// 入力を待つ。
				m_playerAction.setState(PlayerAction.STATE_SUTEHAI_SELECT);
				m_info.postUiEvent(EventId.UI_INPUT_PLAYER_ACTION, a_kazeFrom, a_kazeTo);
				m_playerAction.actionWait();
				if (m_playerAction.isDispMenu()) {

					int menuSelect = m_playerAction.getMenuSelect();
					if ((menuSelect >= 0) && (menuSelect < menuNum)) {
						m_playerAction.init();
						if (eventId[menuSelect] == EventId.REACH) {
							m_playerAction.m_indexs = indexs;
							m_playerAction.m_indexNum = indexNum;
							while (true) {
								// 入力を待つ。
								m_playerAction.setState(PlayerAction.STATE_SUTEHAI_SELECT);
								m_playerAction.actionWait();
								sutehaiIdx = m_playerAction.getSutehaiIdx();
								if (sutehaiIdx != Integer.MAX_VALUE) {
									if (sutehaiIdx >= 0 && sutehaiIdx <= 13) {
										break;
									}
								}
							}
							m_playerAction.init();
							this.m_iSutehai = sutehaiIdx;
						} else if ((eventId[menuSelect] == EventId.ANKAN) ||
								(eventId[menuSelect] == EventId.KAN)) {
							if (kanNum > 1) {
								while (true) {
									m_playerAction.init();
									// 入力を待つ。
									m_playerAction.setChiiEventId(eventId[iChii]);
									m_playerAction.setState(PlayerAction.STATE_KAN_SELECT);
									m_info.postUiEvent(EventId.UI_INPUT_PLAYER_ACTION, a_kazeFrom, a_kazeTo);
									m_playerAction.actionWait();
									int kanSelect = m_playerAction.getKanSelect();
									m_playerAction.init();
									this.m_iSutehai = kanSelect;
									return eventId[menuSelect];
								}
							}
						}
						return eventId[menuSelect];
					}
					m_playerAction.init();
				} else {
					sutehaiIdx = m_playerAction.getSutehaiIdx();
					if (sutehaiIdx != Integer.MAX_VALUE) {
						if (sutehaiIdx >= 0 && sutehaiIdx <= 13) {
							break;
						}
					}
				}
			}
			m_playerAction.init();
			this.m_iSutehai = sutehaiIdx;
			return EventId.SUTEHAI;
		case SELECT_SUTEHAI:
			m_info.copyTehai(m_tehai, m_info.getJikaze());
			jyunTehaiLength = m_tehai.getJyunTehaiLength();
			while (true) {
				// 入力を待つ。
				m_playerAction.setState(PlayerAction.STATE_SUTEHAI_SELECT);
				m_playerAction.actionWait();
				sutehaiIdx = m_playerAction.getSutehaiIdx();
				if (sutehaiIdx != Integer.MAX_VALUE) {
					if (sutehaiIdx >= 0 && sutehaiIdx <= jyunTehaiLength) {
						break;
					}
				}
			}
			Log.d("Man", "sutehaiIdx = " + sutehaiIdx);
			m_playerAction.init();
			this.m_iSutehai = sutehaiIdx;
			return EventId.SUTEHAI;
		case SUTEHAI:
			if (a_kazeFrom == m_info.getJikaze()) {
				return EventId.NAGASHI;
			}
			Log.e("SUTEHAI", "fromKaze = " + a_kazeFrom + ", toKaze = " + a_kazeTo);

			m_info.copyTehai(m_tehai, m_info.getJikaze());
			suteHai = m_info.getSuteHai();

			agariScore = m_info.getAgariScore(m_tehai, suteHai);
			if (agariScore > 0) {
				Log.d("Man", "agariScore = " + agariScore);
				m_playerAction.setValidRon(true);
				eventId[menuNum] = EventId.RON_AGARI;
				menuNum++;
			}

			if (m_tehai.validPon(suteHai)) {
				m_playerAction.setValidPon(true);
				eventId[menuNum] = EventId.PON;
				menuNum++;
			}

			if ((relation == -1) || (relation == 3)) {
				if (m_tehai.validChiiRight(suteHai, sarashiHaiRight)) {
					m_playerAction.setValidChiiRight(true, sarashiHaiRight);
					if (chiiCount == 0) {
						iChii = menuNum;
						eventId[menuNum] = EventId.CHII_RIGHT;
						menuNum++;
					}
					chiiCount++;
				}

				if (m_tehai.validChiiCenter(suteHai, sarashiHaiCenter)) {
					m_playerAction.setValidChiiCenter(true, sarashiHaiCenter);
					if (chiiCount == 0) {
						iChii = menuNum;
						eventId[menuNum] = EventId.CHII_CENTER;
						menuNum++;
					}
					chiiCount++;
				}

				if (m_tehai.validChiiLeft(suteHai, sarashiHaiLeft)) {
					m_playerAction.setValidChiiLeft(true, sarashiHaiLeft);
					if (chiiCount == 0) {
						iChii = menuNum;
						eventId[menuNum] = EventId.CHII_LEFT;
						menuNum++;
					}
					chiiCount++;
				}
			}

			if (m_tehai.validDaiMinKan(suteHai)) {
				m_playerAction.setValidDaiMinKan(true);
				eventId[menuNum] = EventId.DAIMINKAN;
				menuNum++;
			}

			if (menuNum > 0) {
				m_playerAction.setMenuSelect(5);
				m_info.postUiEvent(EventId.UI_INPUT_PLAYER_ACTION, a_kazeFrom, a_kazeTo);
				m_playerAction.actionWait();
				int menuSelect = m_playerAction.getMenuSelect();
				if (menuSelect < menuNum) {
					if ((eventId[menuSelect] == EventId.CHII_LEFT) ||
							(eventId[menuSelect] == EventId.CHII_CENTER) ||
							(eventId[menuSelect] == EventId.CHII_RIGHT)) {
						if (chiiCount > 1) {
							while (true) {
								m_playerAction.init();
								// 入力を待つ。
								m_playerAction.setChiiEventId(eventId[iChii]);
								m_playerAction.setState(PlayerAction.STATE_CHII_SELECT);
								m_info.postUiEvent(EventId.UI_INPUT_PLAYER_ACTION, a_kazeFrom, a_kazeTo);
								m_playerAction.actionWait();
								EventId chiiEventId = m_playerAction.getChiiEventId();
								m_playerAction.init();
								return chiiEventId;
							}
						}
					}
					m_playerAction.init();
					return eventId[menuSelect];
				}
				m_playerAction.init();
			}
			break;
		default:
			break;
		}

		return EventId.NAGASHI;
	}

	public int getISutehai() {
		return m_iSutehai;
	}
}
