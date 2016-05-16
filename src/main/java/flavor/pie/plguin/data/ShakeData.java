package flavor.pie.plguin.data;

import com.google.inject.Inject;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableBooleanData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractBooleanData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class ShakeData extends AbstractBooleanData<ShakeData, ShakeData.ImmutableShakeData> {
    boolean bool;
    Key<Value<Boolean>> key;
    protected ShakeData(boolean value, Key<Value<Boolean>> usedKey) {
        super(value, usedKey, false);
        key = usedKey;
        bool = value;
    }
    @Override
    public Optional<ShakeData> fill(DataHolder dataHolder, MergeFunction overlap) {
        if (dataHolder.supports(key)) {
            Optional<Boolean> bool_ = dataHolder.get(key);
            if (bool_.isPresent()) bool = bool_.get(); else bool = false;
            return Optional.of(this);
        } else
            return Optional.empty();
    }

    @Override
    public Optional<ShakeData> from(DataContainer container) {
        Optional<Boolean> bool_ = container.getBoolean(key.getQuery());
        if (bool_.isPresent()) {
            bool = bool_.get();
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public ShakeData copy() {
        return new ShakeData(bool, key);
    }

    @Override
    public ImmutableShakeData asImmutable() {
        return new ImmutableShakeData(bool, key);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    public static class ImmutableShakeData extends AbstractImmutableBooleanData<ImmutableShakeData, ShakeData> {
        Key<Value<Boolean>> key;
        boolean bool;
        protected ImmutableShakeData(boolean value, Key<Value<Boolean>> usedKey) {
            super(value, usedKey, false);
            key = usedKey;
            bool = value;
        }
        @Override
        public ShakeData asMutable() {
            return new ShakeData(bool, key);
        }

        @Override
        public int getContentVersion() {
            return 1;
        }

    }
    public static class ShakeDataBuilder implements DataManipulatorBuilder<ShakeData, ImmutableShakeData> {
        @Inject
        Key<Value<Boolean>> key;
        @Override
        public ShakeData create() {
            return new ShakeData(false, key);
        }

        @Override
        public Optional<ShakeData> createFrom(DataHolder dataHolder) {
            if (dataHolder.supports(key)) {
                Optional<Boolean> bool_ = dataHolder.get(key);
                return Optional.of(new ShakeData(bool_.orElse(false), key));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Optional<ShakeData> build(DataView container) throws InvalidDataException {
            Optional<Boolean> bool_ = container.getBoolean(key.getQuery());
            if (bool_.isPresent()) {
                return Optional.of(new ShakeData(bool_.get(), key));
            } else
                return Optional.empty();
        }
    }
}
