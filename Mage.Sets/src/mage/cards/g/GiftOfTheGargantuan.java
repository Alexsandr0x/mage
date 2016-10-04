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
package mage.cards.g;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.common.FilterCreatureCard;
import mage.filter.common.FilterLandCard;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;

/**
 *
 * @author North
 */
public class GiftOfTheGargantuan extends CardImpl {

    public GiftOfTheGargantuan(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.SORCERY},"{2}{G}");


        // Look at the top four cards of your library. You may reveal a creature card and/or a land card from among them and put the revealed cards into your hand. Put the rest on the bottom of your library in any order.
        this.getSpellAbility().addEffect(new GiftOfTheGargantuanEffect());
    }

    public GiftOfTheGargantuan(final GiftOfTheGargantuan card) {
        super(card);
    }

    @Override
    public GiftOfTheGargantuan copy() {
        return new GiftOfTheGargantuan(this);
    }
}

class GiftOfTheGargantuanEffect extends OneShotEffect {

    public GiftOfTheGargantuanEffect() {
        super(Outcome.DrawCard);
        this.staticText = "Look at the top four cards of your library. You may reveal a creature card and/or a land card from among them and put the revealed cards into your hand. Put the rest on the bottom of your library in any order";
    }

    public GiftOfTheGargantuanEffect(final GiftOfTheGargantuanEffect effect) {
        super(effect);
    }

    @Override
    public GiftOfTheGargantuanEffect copy() {
        return new GiftOfTheGargantuanEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }

        Cards cards = new CardsImpl();
        boolean creatureCardFound = false;
        boolean landCardFound = false;
        int count = Math.min(player.getLibrary().size(), 4);
        for (int i = 0; i < count; i++) {
            Card card = player.getLibrary().removeFromTop(game);
            if (card != null) {
                cards.add(card);
                if (card.getCardType().contains(CardType.CREATURE)) {
                    creatureCardFound = true;
                }
                if (card.getCardType().contains(CardType.LAND)) {
                    landCardFound = true;
                }
            }
        }
        player.lookAtCards("Gift of the Gargantuan", cards, game);

        if ((creatureCardFound || landCardFound) && player.chooseUse(Outcome.DrawCard, "Do you wish to reveal a creature card and/or a land card and put them into your hand?", source, game)) {
            Cards revealedCards = new CardsImpl();

            TargetCard target = new TargetCard(Zone.LIBRARY, new FilterCreatureCard("creature card to reveal and put into your hand"));
            if (creatureCardFound && player.choose(Outcome.DrawCard, cards, target, game)) {
                Card card = cards.get(target.getFirstTarget(), game);
                if (card != null) {
                    cards.remove(card);
                    card.moveToZone(Zone.HAND, source.getSourceId(), game, false);
                    revealedCards.add(card);
                }
            }

            target = new TargetCard(Zone.LIBRARY, new FilterLandCard("land card to reveal and put into your hand"));
            if (landCardFound && player.choose(Outcome.DrawCard, cards, target, game)) {
                Card card = cards.get(target.getFirstTarget(), game);
                if (card != null) {
                    cards.remove(card);
                    card.moveToZone(Zone.HAND, source.getSourceId(), game, false);
                    revealedCards.add(card);
                }
            }

            if (!revealedCards.isEmpty()) {
                player.revealCards("Gift of the Gargantuan", revealedCards, game);
            }
        }
        player.putCardsOnBottomOfLibrary(cards, game, source, true);
        return true;
    }
}
