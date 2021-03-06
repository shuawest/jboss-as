/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.protocol.mgmt;

import static org.jboss.as.protocol.old.ProtocolUtils.expectHeader;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

/**
 * DomainClientProtocol header used to send the required information to establish a request with a remote host controller.  The primary
 * pieces of the request are the protocol signature and the protocol version being used.
 *
 * @author John Bailey
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
abstract class ManagementProtocolHeader {

    private int version;

    /**
     * Construct an instance with the protocol version for the header.
     *
     * @param version The protocol version
     */
    protected ManagementProtocolHeader(int version) {
        this.version = version;
    }
    /**
     * Write the header information to the provided {@link java.io.DataOutput}.
     *
     * @param output The output to write to
     * @throws IOException If any problems occur writing to the output
     */
    public void write(final DataOutput output) throws IOException {
        output.write(ManagementProtocol.SIGNATURE);
        output.writeByte(ManagementProtocol.VERSION_FIELD);
        output.writeInt(getVersion());
        output.writeByte(ManagementProtocol.TYPE);
        output.writeByte(getType());
    }

    /**
     * The protocol version for the current communication.
     *
     * @return The protocol version
     */
    public int getVersion() {
        return version;
    }

    /**
     * The type
     *
     * @return the protocol byte identifying the type
     */
    abstract byte getType();

    /**
     * Is this a request.
     *
     * @return true if this header is a request; false if it is a response
     */
    boolean isRequest() {
        return getType() == ManagementProtocol.TYPE_REQUEST;
    }

    /**
     * Validate the header signature.
     *
     * @param input The input to read the signature from
     * @throws IOException If any read problems occur
     */
    protected static void validateSignature(final DataInput input) throws IOException, ByeByeException {
        final byte[] signatureBytes = new byte[4];
        byte first = input.readByte();
        if (first == ManagementProtocol.BYE_BYE) {
            throw new ByeByeException();
        }
        signatureBytes[0] = first;
        signatureBytes[1] = input.readByte();
        signatureBytes[2] = input.readByte();
        signatureBytes[3] = input.readByte();
        if (!Arrays.equals(ManagementProtocol.SIGNATURE, signatureBytes)) {
            throw new IOException("Invalid signature [" + Arrays.toString(signatureBytes) + "]");
        }
    }


    /**
     * Parses the input stream to read the header
     *
     */
    static ManagementProtocolHeader parse(DataInput input) throws IOException, ByeByeException {
        validateSignature(input);
        expectHeader(input, ManagementProtocol.VERSION_FIELD);
        int version = input.readInt();
        expectHeader(input, ManagementProtocol.TYPE);
        byte type = input.readByte();
        switch (type) {
            case ManagementProtocol.TYPE_REQUEST:
                return new ManagementRequestHeader(version, input);
            case ManagementProtocol.TYPE_RESPONSE:
                return new ManagementResponseHeader(version, input);
            default:
                throw new IOException("Invalid type: " + type);
        }
    }
}
