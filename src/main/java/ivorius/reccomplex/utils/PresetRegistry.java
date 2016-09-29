/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ivorius.reccomplex.files.*;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by lukas on 26.02.15.
 */
public abstract class PresetRegistry<T>
{
    protected final SimpleLeveledRegistry<Preset<T>> registry;

    protected Gson gson;
    @Nullable
    protected String defaultID;
    protected String fileSuffix;

    public PresetRegistry(String fileSuffix, String description)
    {
        this.fileSuffix = fileSuffix;

        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        registerGson(builder);
        gson = builder.create();

        registry = new SimpleLeveledRegistry<>(description);
    }

    @Nonnull
    public static <T> Preset<T> fullPreset(@Nonnull String id, @Nonnull T t, @Nullable Metadata metadata)
    {
        return new Preset<T>(t, metadata != null ? metadata : new Metadata(id, new String[0]));
    }

    public String getFileSuffix()
    {
        return fileSuffix;
    }

    public SimpleLeveledRegistry<Preset<T>> getRegistry()
    {
        return registry;
    }

    public void register(@Nonnull String id, @Nonnull T t, @Nullable Metadata metadata, LeveledRegistry.Level level)
    {
        registry.register(id, "", fullPreset(id, t, metadata), true, level);
    }

    public void setDefault(@Nonnull String type)
    {
        defaultID = type;
    }

    @Nullable
    public String defaultID()
    {
        return defaultID;
    }

    @Nonnull
    public Optional<T> preset(String id)
    {
        return Optional.ofNullable(registry.get(id))
                .map(p -> copy(p.t));
    }

    public Metadata copy(Metadata meta)
    {
        return gson.fromJson(gson.toJsonTree(meta), Metadata.class);
    }

    public T copy(T t)
    {
        return gson.fromJson(gson.toJsonTree(t), getType());
    }

    @Nonnull
    public Optional<Preset<T>> fullPreset(String id)
    {
        return metadata(id).flatMap(m -> preset(id).map(p -> new Preset<>(p, m)));
    }

    @Nonnull
    public Optional<String> title(String id)
    {
        return metadata(id).map(m -> m.title);
    }

    @Nonnull
    public Optional<List<String>> description(String id)
    {
        return metadata(id).map(d -> d.description).map(Lists::newArrayList);
    }

    @Nonnull
    public Optional<Metadata> metadata(String id)
    {
        return Optional.ofNullable(registry.get(id))
                .map(p -> copy(p.metadata));
    }

    public Collection<String> allIDs()
    {
        return registry.ids();
    }

    public boolean has(String id)
    {
        return registry.has(id);
    }

    protected abstract void registerGson(GsonBuilder builder);

    protected abstract Type getType();

    public FileTypeHandlerRegistryString<Preset<T>> loader()
    {
        return new FileTypeHandlerRegistryString<>(fileSuffix, registry, this::read);
    }

    public Preset<T> read(String file) throws Exception
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(file).getAsJsonObject();

        T t = gson.fromJson(jsonObject.get("data"), getType());
        Metadata metadata = gson.fromJson(jsonObject.get("metadata"), Metadata.class);

        if (t == null || metadata == null)
            throw new ParseException("Error parsing preset", 0);

        return new Preset<>(t, metadata);
    }

    public String write(String id)
    {
        return fullPreset(id).map(p ->
        {
            JsonObject object = new JsonObject();

            object.add("data", gson.toJsonTree(p.t));
            object.add("metadata", gson.toJsonTree(p.metadata));

            return gson.toJson(object);
        }).orElse(null);
    }

    public void save(String id, boolean activeFolder) throws IOException
    {
        File file = FileUtils.getFile(RCFileTypeRegistry.getDirectory(activeFolder), String.format("%s.%s", id, getFileSuffix()));
        if (file.exists())
            file.delete();
        FileUtils.write(file, write(id));
    }

    public static class Metadata
    {
        public String title = "";
        public String[] description;

        public Metadata(String title, String[] description)
        {
            this.title = title;
            this.description = description;
        }

        public Metadata(String title, String description)
        {
            this.title = title;
            this.description = new String[]{description};
        }
    }

    protected static class Preset<T>
    {
        public T t;
        public Metadata metadata;

        public Preset(T t, Metadata metadata)
        {
            this.t = t;
            this.metadata = metadata;
        }
    }
}
