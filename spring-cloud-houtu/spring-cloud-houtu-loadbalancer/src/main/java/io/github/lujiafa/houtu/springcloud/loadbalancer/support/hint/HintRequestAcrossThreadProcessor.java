package io.github.lujiafa.houtu.springcloud.loadbalancer.support.hint;

import io.github.lujiafa.houtu.core.concurrent.AcrossThreadProcessor;

public class HintRequestAcrossThreadProcessor implements AcrossThreadProcessor<String> {

    @Override
    public String parentGet() {
        return HintContext.get().getXHint();
    }

    @Override
    public void childExecuteBefore(Thread parentThread, String value) {
        HintContext.setX(value);
    }

    @Override
    public void childExecuteAfter(Thread parentThread, String value) {
        HintContext.remove();
    }


}
