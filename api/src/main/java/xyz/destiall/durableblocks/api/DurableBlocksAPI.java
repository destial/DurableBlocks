package xyz.destiall.durableblocks.api;

import java.util.concurrent.atomic.AtomicReference;

public interface DurableBlocksAPI {
    AtomicReference<DurableBlocksAPI> inst = new AtomicReference<>(null);
    AtomicReference<NMS> nmst = new AtomicReference<>(null);
    AtomicReference<Manager> player = new AtomicReference<>(null);
    static NMS getNMS() {
        return nmst.get();
    }

    static void setNMS(NMS nms) {
        if (getNMS() == null) {
            nmst.set(nms);
        }
    }

    static void set(DurableBlocksAPI api) {
        if (get() == null) {
            inst.set(api);
        }
    }

    static DurableBlocksAPI get() {
        return inst.get();
    }

    static void setManager(Manager manager) {
        if (getManager() == null) {
            player.set(manager);
        }
    }

    static Manager getManager() {
        return player.get();
    }
}
