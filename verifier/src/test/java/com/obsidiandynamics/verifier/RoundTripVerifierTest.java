package com.obsidiandynamics.verifier;

import org.junit.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.verifier.RoundTripVerifier.*;

public final class RoundTripVerifierTest {
  private static final class JacksonCodec implements Codec {
    private final ObjectMapper mapper;
    
    JacksonCodec(ObjectMapper mapper) {
      this.mapper = mapper;
    }

    @Override
    public byte[] toBytes(Object obj) throws Exception {
      return mapper.writeValueAsBytes(obj);
    }

    @Override
    public <T> T toObject(byte[] bytes, Class<T> type) throws Exception {
      return mapper.readValue(bytes, type);
    }
  }
  
  static final class TestPassPojo {
    @JsonProperty
    int a;
    
    @JsonProperty
    int b;
    
    @Override
    public int hashCode() {
      return 7 * a + 13 * b;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof TestPassPojo) {
        final TestPassPojo that = (TestPassPojo) obj;
        return a == that.a && b == that.b;
      } else {
        return false;
      }
    }
  }
  
  private static Codec getMapperCodec() {
    return new JacksonCodec(new ObjectMapper());
  }
  
  @Test
  public void testPass() {
    RoundTripVerifier.forObject(new TestPassPojo()).withCodec(getMapperCodec()).verify();
  }
  
  @Test
  public void testPassWithType() {
    RoundTripVerifier.forObject(new TestPassPojo()).withType(TestPassPojo.class).withCodec(getMapperCodec()).verify();
  }
  
  static final class UnserializablePojo {
    int a;
  }

  @Test(expected=CodecIOError.class)
  public void testJsonIOError() {
    RoundTripVerifier.forObject(new UnserializablePojo()).withCodec(getMapperCodec()).verify();
  }
  
  static final class TestFailPojo {
    @JsonProperty
    int a;
    
    TestFailPojo(@JsonProperty("a") int a) {}
    
    @Override
    public int hashCode() {
      return a;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof TestFailPojo) {
        final TestFailPojo that = (TestFailPojo) obj;
        return a == that.a;
      } else {
        return false;
      }
    }
  }
  
  @Test(expected=AssertionError.class)
  public void testFailure() {
    final TestFailPojo failPojo = new TestFailPojo(0);
    failPojo.a = 3;
    
    RoundTripVerifier.forObject(failPojo).withCodec(getMapperCodec()).verify();
  }
  
  @Test(expected=NullArgumentException.class)
  public void testWithoutCodec() {
    RoundTripVerifier.forObject("string").verify();
  }
}
