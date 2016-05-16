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

package com.badlogic.invaders;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.utils.Array;
import com.badlogic.invaders.screens.GameLoop;
import com.badlogic.invaders.screens.GameOver;
import com.badlogic.invaders.screens.InvadersScreen;
import com.badlogic.invaders.screens.MainMenu;

public class Invaders extends Game implements ApplicationListener {

	public static InvaderInterface mInvaderInterface;

	/** Music needs to be a class property to prevent being disposed. */
	private Music music;
	private FPSLogger fps;

	private Controller controller;

	public Invaders(InvaderInterface invaderInterface) {
		this.mInvaderInterface = invaderInterface;
	}


	public interface InvaderInterface {
		public double getQ0();
		public double getQ1();
		public double getQ2();
		public double getQ3();
		public long getTimestamp();
	}

	private ControllerAdapter controllerListener = new ControllerAdapter(){
		@Override
		public void connected(Controller c) {
			if (controller == null) {
				controller = c;
			}
		}
		@Override
		public void disconnected(Controller c) {
			if (controller == c) {
				controller = null;
			}
		}
	};

	public Controller getController() {
		return controller;
	}

	@Override
	public void render () {
		InvadersScreen currentScreen = getScreen();

		// update the screen
		currentScreen.render(Gdx.graphics.getDeltaTime());

		// When the screen is done we change to the
		// next screen. Ideally the screen transitions are handled
		// in the screen itself or in a proper state machine.
		if (currentScreen.isDone()) {
			// dispose the resources of the current screen
			currentScreen.dispose();

			// if the current screen is a main menu screen we switch to
			// the game loop
			if (currentScreen instanceof MainMenu) {
				setScreen(new GameLoop(this));
			} else {
				// if the current screen is a game loop screen we switch to the
				// game over screen
				if (currentScreen instanceof GameLoop) {
					setScreen(new GameOver(this));
				} else if (currentScreen instanceof GameOver) {
					// if the current screen is a game over screen we switch to the
					// main menu screen
					setScreen(new MainMenu(this));
				}
			}
		}

		fps.log();
	}

	@Override
	public void create () {
		Array<Controller> controllers = Controllers.getControllers();
		if (controllers.size > 0) {
			controller = controllers.first();
		}
		Controllers.addListener(controllerListener);

		setScreen(new MainMenu(this));
		music = Gdx.audio.newMusic(Gdx.files.getFileHandle("data/8.12.mp3", FileType.Internal));
		music.setLooping(true);
		music.play();
		Gdx.input.setInputProcessor(new InputAdapter() {
			@Override
			public boolean keyUp (int keycode) {
				if (keycode == Keys.ENTER && Gdx.app.getType() == ApplicationType.WebGL) {
					Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
				}
				return true;
			}
		});

		fps = new FPSLogger();
	}

	/** For this game each of our screens is an instance of InvadersScreen.
	 * @return the currently active {@link InvadersScreen}. */
	@Override
	public InvadersScreen getScreen () {
		return (InvadersScreen)super.getScreen();
	}
}



