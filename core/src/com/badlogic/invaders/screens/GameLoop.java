/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.invaders.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.invaders.Invaders;
import com.badlogic.invaders.Renderer;
import com.badlogic.invaders.simulation.Simulation;
import com.badlogic.invaders.simulation.SimulationListener;

public class GameLoop extends InvadersScreen implements SimulationListener {
	/** the simulation **/
	private final Simulation simulation;
	/** the renderer **/
	private final Renderer renderer;
	/** explosion sound **/
	private final Sound explosion;
	/** shot sound **/
	private final Sound shot;

	/** controller **/
	private int buttonsPressed = 0;
	private ControllerListener listener = new ControllerAdapter() {
		@Override
		public boolean buttonDown(Controller controller, int buttonIndex) {
			buttonsPressed++;
			return true;
		}

		@Override
		public boolean buttonUp(Controller controller, int buttonIndex) {
			buttonsPressed--;
			return true;
		}
	};

	public GameLoop (Invaders invaders) {
		super(invaders);
		simulation = new Simulation();
		simulation.listener = this;
		renderer = new Renderer();
		explosion = Gdx.audio.newSound(Gdx.files.internal("data/explosion.wav"));
		shot = Gdx.audio.newSound(Gdx.files.internal("data/shot.wav"));

		if (invaders.getController() != null) {
			invaders.getController().addListener(listener);
		}
	}

	@Override
	public void dispose () {
		renderer.dispose();
		shot.dispose();
		explosion.dispose();
		if (invaders.getController() != null) {
			invaders.getController().removeListener(listener);
		}
		simulation.dispose();
	}

	@Override
	public boolean isDone () {
		return simulation.ship.lives == 0;
	}

	@Override
	public void draw (float delta) {
		renderer.render(simulation, delta);
	}

	@Override
	public void update (float delta) {
		simulation.update(delta);

//		float q1 = BLEDeviceScanActivity.latest_Q0;
//		float q2 = BLEDeviceScanActivity.latest_Q1;
//		float q3 = BLEDeviceScanActivity.latest_Q2;
//		float q4 = BLEDeviceScanActivity.latest_Q3;
//
//		//Equation from Omid's paper with conversions to make the math work
//		double pi_d = Math.PI;
//		float pi_f = (float)pi_d;
//
//		double q1_double = q1;
//		double theta_double = 2*Math.acos(q1_double);
//		float theta = (float)theta_double*180/pi_f;
//
//		double q2_double = q2;
//		double rx_double = -1 * q2_double / Math.sin(theta_double/2);
//		float rx = (float)rx_double;
//
//		double q3_double = q3;
//		double ry_double = -1 * q3_double / Math.sin(theta_double/2);
//		float ry = (float)ry_double;
//
//		double q4_double = q4;
//		double rz_double = -1 * q4_double / Math.sin(theta_double/2);
//		float rz = (float)rz_double;
		float q0 = (float) Invaders.mInvaderInterface.getQ0();
		float q1 = (float) Invaders.mInvaderInterface.getQ1();
		float q2 = (float) Invaders.mInvaderInterface.getQ2();
		float q3 = (float) Invaders.mInvaderInterface.getQ3();

		float accelerometerY = Gdx.input.getAccelerometerY();
//		if (accelerometerY < 0)
		if (0.2f * (q0 * q1 + q2 * q3)>0) {
//			simulation.moveShipLeft(delta, Math.abs(accelerometerY) / 10);
			simulation.moveShipLeft(delta, Math.abs(0.2f * (q0 * q1 + q2 * q3)));
		}
		else
//			simulation.moveShipRight(delta, Math.abs(accelerometerY) / 10);
			simulation.moveShipRight(delta,Math.abs(0.2f * (q0 * q1 + q2 * q3)));

		if (invaders.getController() != null) {
			if (buttonsPressed > 0) {
				simulation.shot();
			}

			// if the left stick moved, move the ship
			float axisValue = invaders.getController().getAxis(Ouya.AXIS_LEFT_X) * 0.5f;
			if (Math.abs(axisValue) > 0.25f) {
				if (axisValue > 0) {
					simulation.moveShipRight(delta, axisValue);
				} else {
					simulation.moveShipLeft(delta, -axisValue);
				}
			}
		}

		if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT) || Gdx.input.isKeyPressed(Keys.A)) simulation.moveShipLeft(delta, 0.5f);
		if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT) || Gdx.input.isKeyPressed(Keys.D)) simulation.moveShipRight(delta, 0.5f);
		if (Gdx.input.isTouched() || Gdx.input.isKeyPressed(Keys.SPACE)) simulation.shot();
	}

	@Override
	public void explosion () {
		explosion.play();
	}

	@Override
	public void shot () {
		shot.play();
	}
}
