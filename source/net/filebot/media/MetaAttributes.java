package net.filebot.media;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import net.filebot.MetaAttributeView;
import net.filebot.vfs.SimpleFileInfo;
import net.filebot.web.AudioTrack;
import net.filebot.web.Episode;
import net.filebot.web.Movie;
import net.filebot.web.MoviePart;
import net.filebot.web.MultiEpisode;

public class MetaAttributes {

	public static final String FILENAME_KEY = "net.filebot.filename";
	public static final String METADATA_KEY = "net.filebot.metadata";

	public static final Map<String, String> JSON_TYPE_MAP = unmodifiableMap(Stream.of(Episode.class, MultiEpisode.class, Movie.class, MoviePart.class, AudioTrack.class, SimpleFileInfo.class).collect(toMap(Class::getName, Class::getSimpleName)));

	private final BasicFileAttributeView fileAttributeView;
	private final MetaAttributeView metaAttributeView;

	public MetaAttributes(File file) throws IOException {
		this.metaAttributeView = new MetaAttributeView(file);
		this.fileAttributeView = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class);
	}

	public void setCreationDate(long millis) throws IOException {
		fileAttributeView.setTimes(null, null, FileTime.fromMillis(millis));
	}

	public long getCreationDate(long time) {
		try {
			return fileAttributeView.readAttributes().creationTime().toMillis();
		} catch (Exception e) {
			return 0;
		}
	}

	public void setOriginalName(String name) {
		metaAttributeView.put(FILENAME_KEY, name);
	}

	public String getOriginalName() {
		return metaAttributeView.get(FILENAME_KEY);
	}

	public void setObject(Object object) {
		try {
			metaAttributeView.put(METADATA_KEY, JsonWriter.objectToJson(object, singletonMap(JsonWriter.TYPE_NAME_MAP, JSON_TYPE_MAP)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object getObject() {
		try {
			String jsonObject = metaAttributeView.get(METADATA_KEY);
			if (jsonObject != null && jsonObject.length() > 0) {
				Map<String, Object> options = new HashMap<String, Object>(2);
				options.put(JsonReader.TYPE_NAME_MAP, JSON_TYPE_MAP);

				// options must be a modifiable map
				return JsonReader.jsonToJava(jsonObject, options);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public void clear() {
		metaAttributeView.put(FILENAME_KEY, null);
		metaAttributeView.put(METADATA_KEY, null);
	}

}
