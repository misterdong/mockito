/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.stubbing;

import static org.mockito.internal.exceptions.Reporter.notAMockPassedToWhenMethod;
import static org.mockito.internal.exceptions.Reporter.notAnException;
import static org.mockito.internal.exceptions.Reporter.nullPassedToWhenMethod;
import static org.mockito.internal.progress.ThreadSafeMockingProgress.mockingProgress;
import static org.mockito.internal.stubbing.answers.DoesNothing.doesNothing;
import static org.mockito.internal.util.MockUtil.isMock;

import java.util.LinkedList;
import java.util.List;

import org.mockito.internal.stubbing.answers.CallsRealMethods;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.internal.stubbing.answers.ThrowsExceptionForClassType;
import org.mockito.internal.util.MockUtil;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

public class StubberImpl implements Stubber {

    private final Strictness strictness;

    public StubberImpl(Strictness strictness) {
        this.strictness = strictness;
    }

    private final List<Answer<?>> answers = new LinkedList<Answer<?>>();

    @Override
    public <T> T when(T mock) {
        if (mock == null) {
            mockingProgress().reset();
            throw nullPassedToWhenMethod();
        }

        if (!isMock(mock)) {
            mockingProgress().reset();
            throw notAMockPassedToWhenMethod();
        }

        MockUtil.getInvocationContainer(mock).setAnswersForStubbing(answers, strictness);

        return mock;
    }

    @Override
    public Stubber doReturn(Object toBeReturned) {
        return doReturnValues(toBeReturned);
    }

    @Override
    public Stubber doReturn(Object toBeReturned, Object... nextToBeReturned) {
        return doReturnValues(toBeReturned).doReturnValues(nextToBeReturned);
    }

    private StubberImpl doReturnValues(Object... toBeReturned) {
        if (toBeReturned == null) {
            answers.add(new Returns(null));
            return this;
        }
        for (Object r : toBeReturned) {
            answers.add(new Returns(r));
        }
        return this;
    }

    @Override
    public Stubber doThrow(Throwable... toBeThrown) {
        if (toBeThrown == null) {
            answers.add(new ThrowsException(null));
            return this;
        }
        for (Throwable throwable : toBeThrown) {
            answers.add(new ThrowsException(throwable));
        }
        return this;
    }

    @Override
    public Stubber doThrow(Class<? extends Throwable> toBeThrown) {
        if (toBeThrown == null) {
            mockingProgress().reset();
            throw notAnException();
        }
        return doAnswer(new ThrowsExceptionForClassType(toBeThrown));
    }

    @Override
    public Stubber doThrow(
            Class<? extends Throwable> toBeThrown, Class<? extends Throwable>... nextToBeThrown) {
        Stubber stubber = doThrow(toBeThrown);

        if (nextToBeThrown == null) {
            mockingProgress().reset();
            throw notAnException();
        }

        for (Class<? extends Throwable> next : nextToBeThrown) {
            stubber = stubber.doThrow(next);
        }
        return stubber;
    }

    @Override
    public Stubber doNothing() {
        answers.add(doesNothing());
        return this;
    }

    @Override
    public Stubber doAnswer(Answer answer) {
        answers.add(answer);
        return this;
    }

    @Override
    public Stubber doCallRealMethod() {
        answers.add(new CallsRealMethods());
        return this;
    }
}
