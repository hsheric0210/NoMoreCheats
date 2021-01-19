package com.eric0210.nomorecheats.api.util;

import java.util.ArrayList;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.checks.combat.AutoClicker;
import com.eric0210.nomorecheats.checks.combat.Criticals;
import com.eric0210.nomorecheats.checks.combat.FastShot;
import com.eric0210.nomorecheats.checks.combat.HitReach;
import com.eric0210.nomorecheats.checks.combat.NoSwing;
import com.eric0210.nomorecheats.checks.combat.killaura.KillauraEvent;
import com.eric0210.nomorecheats.checks.combat.killaura.KillauraPacket;
import com.eric0210.nomorecheats.checks.combat.killaura.KillauraNPC;

import com.eric0210.nomorecheats.checks.movement.Fly;
import com.eric0210.nomorecheats.checks.movement.Ghost;
import com.eric0210.nomorecheats.checks.movement.Move;
import com.eric0210.nomorecheats.checks.movement.Derp;
import com.eric0210.nomorecheats.checks.movement.NoFall;
import com.eric0210.nomorecheats.checks.movement.Clip;
import com.eric0210.nomorecheats.checks.movement.Speed;
import com.eric0210.nomorecheats.checks.movement.Velocity;
import com.eric0210.nomorecheats.checks.player.Exploits;
import com.eric0210.nomorecheats.checks.player.FalsePackets;
import com.eric0210.nomorecheats.checks.player.FastUse;
import com.eric0210.nomorecheats.checks.player.Interact;
import com.eric0210.nomorecheats.checks.player.Inventory;
import com.eric0210.nomorecheats.checks.player.Regen;
import com.eric0210.nomorecheats.checks.player.Scaffold;

public class Checks
{
	public static final ArrayList<Check> checks = new ArrayList<>();
	public static final ArrayList<Check> combat_checks = new ArrayList<>();
	public static final ArrayList<Check> movement_checks = new ArrayList<>();
	public static final ArrayList<Check> exploit_checks = new ArrayList<>();
	public static final ArrayList<Check> other_checks = new ArrayList<>();
	public static NoFall noFall;
	public static Speed speed;
	public static Clip clip;
	public static HitReach hitReach;
	public static Interact interact;
	public static Derp derp;
	public static AutoClicker autoclicker_a;
	public static FastShot fastshot;
	public static Criticals criticals;
	public static KillauraEvent killaura_e;
	public static Ghost ghost;
	public static FalsePackets falsePackets;
	public static Regen regen;
	public static FastUse fastUse;
	public static Scaffold scaffold;
	public static KillauraPacket killaura_p;
	public static KillauraNPC killaura_npc;
	public static Velocity velocity;
	public static Fly fly;
	public static NoSwing noSwing;
	public static Inventory inv;
	public static Move move;
	public static Exploits exploits;

	public static final void initalizeChecks()
	{
		exploit_checks.add(exploits = new Exploits());
		exploit_checks.add(fastshot = new FastShot());
		exploit_checks.add(ghost = new Ghost());
		exploit_checks.add(regen = new Regen());
		combat_checks.add(killaura_e = new KillauraEvent());
		combat_checks.add(killaura_p = new KillauraPacket());
		combat_checks.add(killaura_npc = new KillauraNPC());
		combat_checks.add(autoclicker_a = new AutoClicker());
		combat_checks.add(criticals = new Criticals());
		combat_checks.add(noSwing = new NoSwing());
		combat_checks.add(hitReach = new HitReach());
		movement_checks.add(fly = new Fly());
		movement_checks.add(speed = new Speed());
		movement_checks.add(noFall = new NoFall());
		movement_checks.add(clip = new Clip());
		movement_checks.add(derp = new Derp());
		movement_checks.add(scaffold = new Scaffold());
		movement_checks.add(velocity = new Velocity());
		movement_checks.add(move = new Move());
		movement_checks.add(falsePackets = new FalsePackets());
		other_checks.add(interact = new Interact());
		other_checks.add(fastUse = new FastUse());
		other_checks.add(inv = new Inventory());
		checks.addAll(combat_checks);
		checks.addAll(movement_checks);
		checks.addAll(exploit_checks);
		checks.addAll(other_checks);
	}
}
