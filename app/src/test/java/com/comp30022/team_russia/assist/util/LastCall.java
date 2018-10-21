package com.comp30022.team_russia.assist.util;


import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.verification.ArgumentsAreDifferent;
import org.mockito.internal.debugging.LocationImpl;
import org.mockito.internal.reporting.SmartPrinter;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.internal.verification.argumentmatching.ArgumentMatchingTool;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;
import org.mockito.invocation.MatchableInvocation;
import org.mockito.verification.VerificationMode;

import java.util.List;

import static org.mockito.internal.util.StringUtil.join;

/**
 * Mockito verification mode that verifies the last call to a method.
 * Adapted from https://gist.github.com/passsy/9b27d653e00e88ce48a9dbcb24a5315d
 */
public class LastCall implements VerificationMode {

    public static LastCall lastCall() {
        return new LastCall();
    }

    public void verify(VerificationData data) {
        List<Invocation> invocations = data.getAllInvocations();
        MatchableInvocation matchableInvocation = data.getTarget();
        for (int i = invocations.size() - 1; i >= 0; i--) {
            final Invocation invocation = invocations.get(i);

            if (matchableInvocation.getInvocation().getMethod().equals(invocation.getMethod())) {
                if (!matchableInvocation.matches(invocation)) {
                    // throw
                    argumentsAreDifferent(matchableInvocation, invocation);
                } else {
                    // match
                    return;
                }
            }
        }
        throw new MockitoException("Not invoked at all");
    }

    @Override
    public VerificationMode description(String description) {
        return VerificationModeFactory.description(this, description);
    }

    private void argumentsAreDifferent(String wanted, String actual, Location actualLocation) {
        final String message = join("Argument(s) for last call are different! Wanted:",
            wanted,
            new LocationImpl(),
            "Actual invocation has different arguments:",
            actual,
            actualLocation,
            ""
        );

        throw new ArgumentsAreDifferent(message);
    }

    private void argumentsAreDifferent(MatchableInvocation wanted, Invocation invocation) {
        final Integer[] indicesOfSimilarMatchingArguments =
            ArgumentMatchingTool
                .getSuspiciouslyNotMatchingArgsIndexes(wanted.getMatchers(),
                    invocation.getArguments());
        final SmartPrinter smartPrinter = new SmartPrinter(wanted, invocation,
            indicesOfSimilarMatchingArguments);
        argumentsAreDifferent(smartPrinter.getWanted(), smartPrinter.getActual(),
            invocation.getLocation());
    }
}
