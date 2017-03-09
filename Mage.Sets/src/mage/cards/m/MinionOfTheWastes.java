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

import java.util.UUID;
import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.game.Game;
import mage.players.Player;

/**
 *
 * @author Alexsandro
 */
public class MinionOfTheWastes extends CardImpl {

    public MinionOfTheWastes(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{B}{B}{B}");
        
        this.subtype.add("Minion");

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // As Minion of the Wastes enters the battlefield, pay any amount of life.
        // Minion of the Wastes's power and toughness are each equal to the life paid as it entered the battlefield.
        this.addAbility(new EntersBattlefieldAbility(new MinionOfTheWastesEffect()));
    }

    public MinionOfTheWastes(final MinionOfTheWastes card) {
        super(card);
    }

    @Override
    public MinionOfTheWastes copy() {
        return new MinionOfTheWastes(this);
    }

    class MinionOfTheWastesEffect extends OneShotEffect {

        private int paidLife;

        public MinionOfTheWastesEffect() {
            super(Outcome.Neutral);
            this.staticText = "pay any amount of life. Minion of the Wastes's power and toughness are each equal to " +
                    "the life paid as it entered the battlefield.";
        }

        @Override
        public boolean apply(Game game, Ability source) {
            Player controller = game.getPlayer(source.getControllerId());
            Card sourceCard = game.getCard(source.getSourceId());

            if (controller != null && sourceCard != null) {
                MageObject target = game.getObject(source.getSourceId());

                paidLife = controller.getAmount(0, controller.getLife(),"Pay how many life?", game);
                if (paidLife > 0) {
                    controller.loseLife(paidLife, game, false);
                    game.informPlayers(new StringBuilder(sourceCard.getName())
                            .append(": ").append(controller.getLogName())
                            .append(" pays ").append(paidLife).append(" life").toString());

                    target.getPower().boostValue(paidLife);
                    target.getToughness().boostValue(paidLife);
                }
                return true;
            }
            return false;
        }

        @Override
        public Effect copy() {
            return new MinionOfTheWastesEffect();
        }
    }
}
