package com.github.hanyaeger.api.scenes;

import com.github.hanyaeger.core.YaegerConfig;
import com.github.hanyaeger.core.entities.Debugger;
import com.github.hanyaeger.core.entities.EntitySupplier;
import com.github.hanyaeger.core.repositories.DragNDropRepository;
import com.github.hanyaeger.core.scenes.delegates.BackgroundDelegate;
import com.github.hanyaeger.core.scenes.delegates.KeyListenerDelegate;
import com.github.hanyaeger.core.factories.EntityCollectionFactory;
import com.github.hanyaeger.core.factories.SceneFactory;
import com.google.inject.Injector;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.github.hanyaeger.core.entities.EntityCollection;
import com.github.hanyaeger.api.entities.YaegerEntity;
import com.github.hanyaeger.api.userinput.KeyListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StaticSceneTest {
    private StaticSceneImpl sut;
    private SceneFactory sceneFactory;
    private EntityCollectionFactory entityCollectionFactory;

    private KeyListenerDelegate keyListenerDelegate;
    private BackgroundDelegate backgroundDelegate;
    private DragNDropRepository dragNDropRepository;
    private Debugger debugger;
    private Injector injector;

    private EntityCollection entityCollection;
    private EntitySupplier entitySupplier;
    private Pane pane;
    private Scene scene;
    private Stage stage;
    private YaegerConfig config;

    @BeforeEach
    void setup() {
        sut = new StaticSceneImpl();

        pane = mock(Pane.class, withSettings().withoutAnnotations());
        backgroundDelegate = mock(BackgroundDelegate.class);
        keyListenerDelegate = mock(KeyListenerDelegate.class);
        dragNDropRepository = mock(DragNDropRepository.class);
        debugger = mock(Debugger.class);
        entitySupplier = mock(EntitySupplier.class);
        sceneFactory = mock(SceneFactory.class);
        entityCollectionFactory = mock(EntityCollectionFactory.class);
        injector = mock(Injector.class);
        stage = mock(Stage.class);
        config = mock(YaegerConfig.class);

        sut.setDebugger(debugger);
        sut.setSceneFactory(sceneFactory);
        sut.setEntityCollectionFactory(entityCollectionFactory);
        sut.setDragNDropRepository(dragNDropRepository);
        sut.setPane(pane);
        sut.setBackgroundDelegate(backgroundDelegate);
        sut.setKeyListenerDelegate(keyListenerDelegate);
        sut.setEntitySupplier(entitySupplier);
        sut.setStage(stage);
        sut.setConfig(config);

        scene = mock(Scene.class);
        entityCollection = mock(EntityCollection.class);

        when(sceneFactory.create(pane)).thenReturn(scene);
        when(entityCollectionFactory.create(pane, config)).thenReturn(entityCollection);

        sut.init(injector);
    }

    @Test
    void getInjectorReturnsInjectorProvidedThroughInit() {
        // Arrange

        // Act
        var actual = sut.getInjector();

        // Verify
        assertEquals(actual, injector);
    }

    @Test
    void getStageReturnsSetStage() {
        // Arrange

        // Act
        var actual = sut.getStage();

        // Verify
        assertEquals(actual, stage);
    }

    @Test
    void getDragNDropRepositoryReturnsDragNDropRepository() {
        // Arrange

        // Act
        var actual = sut.getDragNDropRepository();

        // Assert
        assertEquals(actual, dragNDropRepository);
    }

    @Test
    void getEntityCollectionReturnsEntityCollection() {
        // Arrange
        sut.activate();

        // Act
        var actual = sut.getEntityCollection();

        // Assert
        assertEquals(actual, entityCollection);
    }

    @Test
    void getNodeReturnsOptionalOfParentNode() {
        // Arrange
        sut.activate();

        var expected = mock(Parent.class, withSettings().withoutAnnotations());
        when(sut.getScene().getRoot()).thenReturn(expected);

        // Act
        var actual = sut.getNode();

        // Assert
        assertEquals(expected, actual.get());
    }

    @Test
    void activateCreatesAScene() {
        // Arrange

        // Act
        sut.activate();

        // Verify
        verify(sceneFactory).create(pane);
    }


    @Test
    void activateSetsUpADebuggerIfConfigHasShowDebug() {
        // Arrange
        when(config.showDebug()).thenReturn(true);

        // Act
        sut.activate();

        // Verify
        verify(debugger).setup(pane);
    }


    @Test
    void activateCreatesAnEntityCollection() {
        // Arrange

        // Act
        sut.activate();

        // Verify
        verify(entityCollectionFactory).create(pane, config);
    }

    @Test
    void activateInjectDependenciesIntoEntityCollection() {
        // Arrange

        // Act
        sut.activate();

        // Verify
        verify(injector).injectMembers(any());
    }

    @Test
    void activateSetsUpAKeyListenerDelegate() {
        // Arrange

        // Act
        sut.activate();

        // Verify
        verify(keyListenerDelegate).setup(any(Scene.class), any(KeyListener.class));
    }

    @Test
    void activateAddsTheDebuggerAsAStatisticsObserverToTheEntityCollection() {
        // Arrange
        var entityCollection = mock(EntityCollection.class);
        when(entityCollectionFactory.create(pane, config)).thenReturn(entityCollection);

        when(config.showDebug()).thenReturn(true);

        // Act
        sut.activate();

        // Verify
        verify(entityCollection).addStatisticsObserver(debugger);
    }

    @Test
    void destroyDelegatesDestroy() {
        // Arrange
        var children = mock(ObservableList.class);
        when(pane.getChildren()).thenReturn(children);

        sut.activate();

        // Act
        sut.destroy();

        // Verify
        verify(keyListenerDelegate).tearDown(scene);
        verify(backgroundDelegate).destroy();
        verify(children).clear();
    }

    @Test
    void addEntityAddsTheEntitySupplier() {
        // Arrange
        sut.activate();

        var testEntity = mock(YaegerEntity.class);

        // Act
        sut.addEntity(testEntity);

        // Verify
        verify(entitySupplier).add(testEntity);
    }

    @Test
    void setBackgroundAudioDelegatesToBackgroundDelegate() {
        // Arrange
        final var AUDIO_STRING = "Hello World";

        // Act
        sut.setBackgroundAudio(AUDIO_STRING);

        // Verify
        verify(backgroundDelegate).setBackgroundAudio(AUDIO_STRING);
    }

    @Test
    void implementingKeyListenerAddsSceneToKeyListeners() {
        // Arrange
        final var sut = new StaticSceneKeyListenerImpl();

        sut.setDebugger(debugger);
        sut.setSceneFactory(sceneFactory);
        sut.setEntityCollectionFactory(entityCollectionFactory);
        sut.setPane(pane);
        sut.setBackgroundDelegate(backgroundDelegate);
        sut.setKeyListenerDelegate(keyListenerDelegate);
        sut.setEntitySupplier(entitySupplier);
        sut.setStage(stage);
        sut.setConfig(config);

        scene = mock(Scene.class);
        entityCollection = mock(EntityCollection.class);

        when(sceneFactory.create(pane)).thenReturn(scene);
        when(entityCollectionFactory.create(pane, config)).thenReturn(entityCollection);

        sut.init(injector);

        // Act
        sut.activate();

        // Verify
        verify(entityCollection).registerKeyListener(sut);
    }

    @Test
    void setBackgroundColorDelegatesToBackgroundDelegate() {
        // Arrange
        final var color = Color.YELLOW;

        // Act
        sut.setBackgroundColor(color);

        // Verify
        verify(backgroundDelegate).setBackgroundColor(color);
    }

    @Test
    void setBackgroundImageDelegatesToBackgroundDelegateFullscreen() {
        // Arrange
        final var IMAGE_STRING = "Hello World";

        // Act
        sut.setBackgroundImage(IMAGE_STRING);

        // Verify
        verify(backgroundDelegate).setBackgroundImage(IMAGE_STRING, true);
    }

    @Test
    void setFullscreenBackgroundImageDelegatesToBackgroundDelegateFullscreen() {
        // Arrange
        final var IMAGE_STRING = "Hello World";

        // Act
        sut.setBackgroundImage(IMAGE_STRING, true);

        // Verify
        verify(backgroundDelegate).setBackgroundImage(IMAGE_STRING, true);
    }

    @Test
    void setTiledBackgroundImageDelegatesToBackgroundDelegateTiled() {
        // Arrange
        final var IMAGE_STRING = "Hello World";

        // Act
        sut.setBackgroundImage(IMAGE_STRING, false);

        // Verify
        verify(backgroundDelegate).setBackgroundImage(IMAGE_STRING, false);
    }

    @Test
    void stopBackgroundAudioVolumeDelegatesToBackgroundDelegate() {
        // Arrange

        // Act
        sut.stopBackgroundAudio();

        // Verify
        verify(backgroundDelegate).stopBackgroundAudio();
    }

    @Test
    void setBackgroundAudioVolumeDelegatesToBackgroundDelegate() {
        // Arrange
        final var expected = 0.37D;

        // Act
        sut.setBackgroundAudioVolume(expected);

        // Verify
        verify(backgroundDelegate).setVolume(expected);
    }

    @Test
    void getBackgroundAudioVolumeDelegatesToBackgroundDelegate() {
        // Arrange
        final var expected = 0.37D;

        when(backgroundDelegate.getVolume()).thenReturn(expected);

        // Act
        var actual = sut.getBackgroundAudioVolume();

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    void getSceneReturnsExpectedScene() {
        // Arrange
        sut.activate();

        // Act
        var returnedScene = sut.getScene();

        // Verify
        assertEquals(scene, returnedScene);
    }

    @Test
    void getTileMapsReturnsNotNullArrayList() {
        // Arrange
        sut.activate();

        // Act
        var actual = sut.getTileMaps();

        // Verify
        assertNotNull(actual);
    }

    @Test
    void getEntitySpawnersReturnsExpectedEntitySpawners() {
        // Arrange
        sut.activate();

        // Act
        var actual = sut.getEntitySupplier();

        // Verify
        assertNotNull(actual);
    }

    @Test
    void onInputChangeNotifiesEntityCollection() {
        // Arrange
        var input = new HashSet<KeyCode>();
        input.add(KeyCode.A);

        sut.activate();
        ArgumentCaptor<KeyListener> captor = ArgumentCaptor.forClass(KeyListener.class);
        verify(keyListenerDelegate, times(1)).setup(any(), captor.capture());

        // Act
        captor.getValue().onPressedKeysChange(input);

        // Verify
        verify(entityCollection).notifyGameObjectsOfPressedKeys(input);
    }

    @Test
    void postActivationMakesRequiredCalls() {
        // Arrange
        sut.activate();

        // Act
        sut.postActivate();

        // Verify
        verify(entityCollection).registerSupplier(any());
        verify(entityCollection).initialUpdate();
    }

    @Test
    void postActivationHidesAndShowsStage() {
        // Arrange
        sut.activate();

        // Act
        sut.postActivate();

        // Verify
        verify(stage).hide();
        verify(stage).show();
    }

    @Test
    void postActivationCalssPostActivationOnDebugger() {
        // Arrange
        sut.activate();
        when(config.showDebug()).thenReturn(true);

        // Act
        sut.postActivate();

        // Verify
        verify(debugger).postActivation();
    }

    @Test
    void postActivateSetsIsActivationCompleteToTrue() {
        // Arrange
        sut.activate();

        // Act
        sut.postActivate();

        // Verify
        assertTrue(sut.isActivationComplete());
    }

    @Nested
    class EffectableTests {

        private static final double BRIGHTNESS = 0.37D;
        private static final double CONTRAST = 0.314159D;
        private static final double HUE = 0.42D;
        private static final double SATURATION = 0.27D;

        @Test
        void setBrightnessDelegatesToTheColorAdjust() {
            // Arrange
            var colorAdjust = mock(ColorAdjust.class);
            sut.setColorAdjust(colorAdjust);

            // Act
            sut.setBrightness(BRIGHTNESS);

            // Verify
            verify(colorAdjust).setBrightness(BRIGHTNESS);
        }

        @Test
        void getBrightnessDelegatesToTheColorAdjust() {
            // Arrange
            var colorAdjust = mock(ColorAdjust.class);
            sut.setColorAdjust(colorAdjust);
            when(colorAdjust.getBrightness()).thenReturn(BRIGHTNESS);

            // Act
            double actual = sut.getBrightness();

            // Verify
            assertEquals(BRIGHTNESS, actual);
        }

        @Test
        void setContrastDelegatesToTheColorAdjust() {
            // Arrange
            var colorAdjust = mock(ColorAdjust.class);
            sut.setColorAdjust(colorAdjust);

            // Act
            sut.setContrast(CONTRAST);

            // Verify
            verify(colorAdjust).setContrast(CONTRAST);
        }

        @Test
        void getConstrastDelegatesToTheColorAdjust() {
            // Arrange
            var colorAdjust = mock(ColorAdjust.class);
            sut.setColorAdjust(colorAdjust);
            when(colorAdjust.getContrast()).thenReturn(CONTRAST);

            // Act
            double actual = sut.getContrast();

            // Verify
            assertEquals(CONTRAST, actual);
        }

        @Test
        void setHueDelegatesToTheColorAdjust() {
            // Arrange
            var colorAdjust = mock(ColorAdjust.class);
            sut.setColorAdjust(colorAdjust);

            // Act
            sut.setHue(HUE);

            // Verify
            verify(colorAdjust).setHue(HUE);
        }

        @Test
        void getHueDelegatesToTheColorAdjust() {
            // Arrange
            var colorAdjust = mock(ColorAdjust.class);
            sut.setColorAdjust(colorAdjust);
            when(colorAdjust.getHue()).thenReturn(HUE);

            // Act
            double actual = sut.getHue();

            // Verify
            assertEquals(HUE, actual);
        }

        @Test
        void setSaturationDelegatesToTheColorAdjust() {
            // Arrange
            var colorAdjust = mock(ColorAdjust.class);
            sut.setColorAdjust(colorAdjust);

            // Act
            sut.setSaturation(SATURATION);

            // Verify
            verify(colorAdjust).setSaturation(SATURATION);
        }

        @Test
        void getSaturationDelegatesToTheColorAdjust() {
            // Arrange
            var colorAdjust = mock(ColorAdjust.class);
            sut.setColorAdjust(colorAdjust);
            when(colorAdjust.getSaturation()).thenReturn(SATURATION);

            // Act
            double actual = sut.getSaturation();

            // Verify
            assertEquals(SATURATION, actual);
        }
    }

    private static class StaticSceneImpl extends StaticScene {

        @Override
        public void setupScene() {
        }

        @Override
        public void setupEntities() {
        }
    }

    private static class StaticSceneKeyListenerImpl extends StaticScene implements KeyListener {

        @Override
        public void onPressedKeysChange(Set<KeyCode> pressedKeys) {

        }

        @Override
        public void setupScene() {

        }

        @Override
        public void setupEntities() {

        }
    }
}
