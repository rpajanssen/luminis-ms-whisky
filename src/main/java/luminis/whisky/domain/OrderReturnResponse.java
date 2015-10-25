package luminis.whisky.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement
public class OrderReturnResponse {
    @XmlElement
    private String orderNumber;

    @XmlElement
    private String state;

    public OrderReturnResponse() {}

    public OrderReturnResponse(OrderReturnRequest returnRequest) {
        this.orderNumber = returnRequest.getOrderNumber();
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public OrderReturnResponse withState(String state) {
        this.state = state;
        return this;
    }
}
