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
package mage.cards.s;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.permanent.ControllerIdPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.GameEvent.EventType;
import mage.game.permanent.Permanent;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.target.common.TargetCreaturePermanent;
import mage.target.targetpointer.FixedTarget;

/**
 *
 * @author LevelX2
 */
public class SatyrFiredancer extends CardImpl {

    public SatyrFiredancer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ENCHANTMENT,CardType.CREATURE},"{1}{R}");
        this.subtype.add("Satyr");

        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Whenever an instant or sorcery spell you control deals damage to an opponent, Satyr Firedancer deals that much damage to target creature that player controls.
        this.addAbility(new SatyrFiredancerTriggeredAbility());
    }

    public SatyrFiredancer(final SatyrFiredancer card) {
        super(card);
    }
    
    @Override
    public void adjustTargets(Ability ability, Game game) {
        if (ability instanceof SatyrFiredancerTriggeredAbility) {
            Player opponent = game.getPlayer(ability.getEffects().get(0).getTargetPointer().getFirst(game, ability));
            if (opponent != null) {
                FilterCreaturePermanent filter = new FilterCreaturePermanent("creature controlled by " + opponent.getLogName());
                filter.add(new ControllerIdPredicate(opponent.getId()));
                ability.getTargets().add(new TargetCreaturePermanent(filter));
            }
        }
    }
    
    @Override
    public SatyrFiredancer copy() {
        return new SatyrFiredancer(this);
    }
}

class SatyrFiredancerTriggeredAbility extends TriggeredAbilityImpl {

    private List<UUID> handledStackObjects = new ArrayList<>();

    public SatyrFiredancerTriggeredAbility() {
        super(Zone.BATTLEFIELD, new SatyrFiredancerDamageEffect(), false);
    }

    public SatyrFiredancerTriggeredAbility(final SatyrFiredancerTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public SatyrFiredancerTriggeredAbility copy() {
        return new SatyrFiredancerTriggeredAbility(this);
    }

    @Override
    public void reset(Game game) {
        handledStackObjects.clear();
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == EventType.DAMAGED_PLAYER;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (getControllerId().equals(game.getControllerId(event.getSourceId()))) {
            MageObject damageSource = game.getObject(event.getSourceId());
            if (damageSource != null) {            
                if (game.getOpponents(getControllerId()).contains(event.getTargetId())) {
                    MageObject object = game.getObject(event.getSourceId());
                    if (object.getCardType().contains(CardType.INSTANT) || object.getCardType().contains(CardType.SORCERY)) { 
                        if (!(damageSource instanceof StackObject) || !handledStackObjects.contains(damageSource.getId())) {
                            if (damageSource instanceof StackObject) {
                                handledStackObjects.add(damageSource.getId());
                            }
                            for (Effect effect: this.getEffects()) {
                                effect.setTargetPointer(new FixedTarget(event.getTargetId())); // used by adjust targets
                                effect.setValue("damage", event.getAmount());
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String getRule() {
        return new StringBuilder("Whenever an instant or sorcery spell you control deals damage to an opponent, ").append(super.getRule()).toString();
    }
}

class SatyrFiredancerDamageEffect extends OneShotEffect {

    public SatyrFiredancerDamageEffect() {
        super(Outcome.Damage);
        this.staticText = "{this} deals that much damage to target creature that player controls";
    }

    public SatyrFiredancerDamageEffect(final SatyrFiredancerDamageEffect effect) {
        super(effect);
    }

    @Override
    public SatyrFiredancerDamageEffect copy() {
        return new SatyrFiredancerDamageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent targetCreature = game.getPermanent(source.getFirstTarget());
        Player controller = game.getPlayer(source.getControllerId());
        if (targetCreature != null && controller != null) {
            int damage = (Integer) this.getValue("damage");
            if (damage > 0) {
                targetCreature.damage(damage, source.getSourceId(), game, false, true);                
            }
            return true;
        }
        return false;
    }
}
