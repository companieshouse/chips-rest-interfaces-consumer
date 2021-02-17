package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.service.ServiceException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ErrorConsumerImplTest {

    private static final String DATA = "DATA";
    private static final String ERROR_CONSUMER_ID = "error-consumer";

    @Mock
    private MessageProcessorService messageProcessorService;

    @Mock
    private ApplicationLogger logger;

    @InjectMocks
    private ErrorConsumerImpl errorConsumer;

    private ChipsRestInterfacesSend data;

    @BeforeEach
    void init() {
        data = new ChipsRestInterfacesSend();
        data.setData(DATA);
    }

    @Test
    void readAndProcessErrorTopic() throws ServiceException {
        errorConsumer.readAndProcessErrorTopic(data, 0L, 0, ERROR_CONSUMER_ID);

        verify(messageProcessorService, times(1)).processMessage(ERROR_CONSUMER_ID, data);
    }
}