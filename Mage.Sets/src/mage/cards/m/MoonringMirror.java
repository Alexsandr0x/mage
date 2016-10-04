/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.cards.m;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.BeginningOfUpkeepTriggeredAbility;
import mage.abilities.common.DrawCardControllerTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;

/**
 *
 * @author LevelX2
 */
public class MoonringMirror extends CardImpl {

    protected static final String VALUE_PREFIX = "ExileZones";

    public MoonringMirror(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{5}");

        // Whenever you draw a card, exile the top card of your library face down.
        this.addAbility(new DrawCardControllerTriggeredAbility(new MoonringMirrorExileEffect(), false));

        // At the beginning of your upkeep, you may exile all cards from your hand face down. If you do, put all other cards you own exiled with Moonring Mirror into your hand.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(new MoonringMirrorEffect(), TargetController.YOU, true));
    }

    public MoonringMirror(final MoonringMirror card) {
        super(card);
    }

    @Override
    public MoonringMirror copy() {
        return new MoonringMirror(this);
    }
}

class MoonringMirrorExileEffect extends OneShotEffect {

    public MoonringMirrorExileEffect() {
        super(Outcome.Discard);
        staticText = "exile the top card of your library face down";
    }

    public MoonringMirrorExileEffect(final MoonringMirrorExileEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            Card card = controller.getLibrary().getFromTop(game);
            MageObject sourceObject = source.getSourceObject(game);
            if (card != null && sourceObject != null) {
                UUID exileZoneId = CardUtil.getExileZoneId(game, source.getSourceId(), source.getSourceObjectZoneChangeCounter());
                card.setFaceDown(true, game);
                controller.moveCardsToExile(card, source, game, false, exileZoneId, sourceObject.getIdName());
                card.setFaceDown(true, game);
                Set<UUID> exileZones = (Set<UUID>) game.getState().getValue(MoonringMirror.VALUE_PREFIX + source.getSourceId().toString());
                if (exileZones == null) {
                    exileZones = new HashSet<>();
                    game.getState().setValue(MoonringMirror.VALUE_PREFIX + source.getSourceId().toString(), exileZones);
                }
                exileZones.add(exileZoneId);
                return true;
            }
        }
        return false;
    }

    @Override
    public MoonringMirrorExileEffect copy() {
        return new MoonringMirrorExileEffect(this);
    }
}

class MoonringMirrorEffect extends OneShotEffect {

    public MoonringMirrorEffect() {
        super(Outcome.Benefit);
        this.staticText = "you may exile all cards from your hand face down. If you do, put all other cards you own exiled with {this} into your hand";
    }

    public MoonringMirrorEffect(final MoonringMirrorEffect effect) {
        super(effect);
    }

    @Override
    public MoonringMirrorEffect copy() {
        return new MoonringMirrorEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceObject = source.getSourceObject(game);
        if (controller != null && sourceObject != null) {
            UUID exileZoneId = CardUtil.getExileZoneId(game, source.getSourceId(), source.getSourceObjectZoneChangeCounter());
            Cards cardsToHand = null;
            if (game.getExile().getExileZone(exileZoneId) != null && game.getExile().getExileZone(exileZoneId).size() > 0) {
                cardsToHand = new CardsImpl(game.getExile().getExileZone(exileZoneId));
            }
            for (Card card : controller.getHand().getCards(game)) {
                card.setFaceDown(true, game);
            }
            controller.moveCardsToExile(controller.getHand().getCards(game), source, game, false, exileZoneId, sourceObject.getIdName());
            if (cardsToHand != null) {
                controller.moveCards(cardsToHand.getCards(game), Zone.HAND, source, game, false, true, false, null);
            }
            if (game.getExile().getExileZone(exileZoneId) != null) {
                for (Card card : game.getExile().getExileZone(exileZoneId).getCards(game)) {
                    card.setFaceDown(true, game);
                }
            }
            return true;
        }
        return false;
    }
}
