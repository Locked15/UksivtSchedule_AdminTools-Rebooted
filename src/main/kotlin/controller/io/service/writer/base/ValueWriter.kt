package controller.io.service.writer.base

interface ValueWriter {

    companion object {

        /**
         * Contains writer buffer size, to prevent data-loss on writing big objects.
         * Through the limitations of the platform, this is marked as 'public', but prefers to be 'protected'.
         */
        const val WRITER_BUFFER_SIZE = 4096
    }
}
