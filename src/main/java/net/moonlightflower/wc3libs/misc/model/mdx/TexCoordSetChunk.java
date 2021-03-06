package net.moonlightflower.wc3libs.misc.model.mdx;

import net.moonlightflower.wc3libs.bin.BinInputStream;
import net.moonlightflower.wc3libs.bin.BinStream;
import net.moonlightflower.wc3libs.bin.Wc3BinInputStream;
import net.moonlightflower.wc3libs.bin.Wc3BinOutputStream;
import net.moonlightflower.wc3libs.misc.Id;
import net.moonlightflower.wc3libs.misc.ObservableArrayList;
import net.moonlightflower.wc3libs.misc.model.MDX;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class TexCoordSetChunk extends Chunk {
    public static Id TOKEN = Id.valueOf("UVAS");

    @Override
    public Id getToken() {
        return TOKEN;
    }

    private final ArrayList<TexCoordSet> _texCoordSets = new ObservableArrayList<>();

    public ArrayList<TexCoordSet> getTexCoordSets() {
        return _texCoordSets;
    }

    @Override
    public void write(@Nonnull Wc3BinOutputStream stream, @Nonnull MDX.EncodingFormat format) throws BinInputStream.StreamException {
        stream.writeId(TOKEN);

        stream.writeUInt32(_texCoordSets.size());

        for (TexCoordSet texCoordSet : _texCoordSets) {
            texCoordSet.write(stream);
        }
    }

    @Override
    public void write(@Nonnull Wc3BinOutputStream stream) throws BinStream.StreamException {
        write(stream, MDX.EncodingFormat.AUTO);
    }

    public TexCoordSetChunk(@Nonnull Wc3BinInputStream stream) throws BinStream.StreamException {
        Id token = stream.readId("token");

        if (!token.equals(getToken())) throw new IllegalArgumentException("invalid " + getToken() + " startToken (" + token + ")");

        long matrixGroupsCount = stream.readUInt32("texCoordSetCount");

        while (matrixGroupsCount > 0) {
            _texCoordSets.add(new TexCoordSet(stream));

            matrixGroupsCount--;
        }
    }

    public TexCoordSetChunk() {
    }
}
